package com.library.service;

import com.library.dto.borrow.BorrowRequest;
import com.library.dto.borrow.BorrowResponse;
import com.library.dto.borrow.ReturnResponse;
import com.library.entity.Book;
import com.library.entity.BorrowTransaction;
import com.library.entity.User;
import com.library.entity.enums.BorrowStatus;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BorrowTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing book borrow and return operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private final BorrowTransactionRepository borrowTransactionRepository;
    private final BookService bookService;
    private final UserService userService;
    private final ReservationService reservationService;

    @Value("${app.borrow.max-days:14}")
    private int maxBorrowDays;

    @Value("${app.borrow.max-books-per-student:3}")
    private int maxBooksPerStudent;

    /**
     * Borrow a book for the authenticated user.
     */
    @Transactional
    public BorrowResponse borrowBook(String username, BorrowRequest request) {
        User user = userService.findByUsername(username);
        Book book = bookService.findBookById(request.getBookId());

        // Validate: book must be active
        if (!book.getActive()) {
            throw new BadRequestException("This book is no longer available in the catalog.");
        }

        // Validate: book must have available copies
        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException(
                    "No available copies of '" + book.getTitle() + "'. You can create a reservation instead.");
        }

        // Validate: user hasn't already borrowed this book
        if (borrowTransactionRepository.existsByUserIdAndBookIsbnAndStatus(
                user.getId(), book.getIsbn(), BorrowStatus.BORROWED)) {
            throw new BadRequestException("You already have an active borrow for this book.");
        }

        // Validate: student borrow limit
        long activeBorrows = borrowTransactionRepository.countActiveBorrowsByUser(user.getId());
        if (activeBorrows >= maxBooksPerStudent) {
            throw new BadRequestException(
                    "You have reached the maximum borrow limit (" + maxBooksPerStudent + " books).");
        }

        // Create borrow transaction
        LocalDateTime now = LocalDateTime.now();
        BorrowTransaction transaction = BorrowTransaction.builder()
                .user(user)
                .book(book)
                .borrowDate(now)
                .dueDate(now.plusDays(maxBorrowDays))
                .status(BorrowStatus.BORROWED)
                .build();

        borrowTransactionRepository.save(transaction);

        // Decrease available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);

        log.info("User '{}' borrowed book '{}' (ISBN: {}). Due date: {}",
                username, book.getTitle(), book.getIsbn(), transaction.getDueDate());

        return mapToResponse(transaction);
    }

    /**
     * Return a borrowed book.
     */
    @Transactional
    public ReturnResponse returnBook(String username, Long transactionId) {
        BorrowTransaction transaction = borrowTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", transactionId));

        // Validate: transaction belongs to the user (or user is LIBRARIAN/ADMIN)
        User user = userService.findByUsername(username);
        if (!transaction.getUser().getId().equals(user.getId()) &&
                !user.getRole().name().equals("ADMIN") &&
                !user.getRole().name().equals("LIBRARIAN")) {
            throw new BadRequestException("You can only return your own borrowed books.");
        }

        // Validate: book must be in BORROWED or OVERDUE status
        if (transaction.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException("This book has already been returned.");
        }

        // Process return
        LocalDateTime now = LocalDateTime.now();
        boolean wasOverdue = now.isAfter(transaction.getDueDate());

        transaction.setReturnDate(now);
        transaction.setStatus(BorrowStatus.RETURNED);
        borrowTransactionRepository.save(transaction);

        // Increase available copies
        Book book = transaction.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        // Check and fulfill pending reservations for this book
        reservationService.fulfillNextReservation(book.getIsbn());

        log.info("User '{}' returned book '{}' (ISBN: {}). Overdue: {}",
                username, book.getTitle(), book.getIsbn(), wasOverdue);

        String message = wasOverdue
                ? "Book returned successfully. Note: This book was overdue."
                : "Book returned successfully. Thank you!";

        return ReturnResponse.builder()
                .transactionId(transaction.getTransactionId())
                .bookTitle(book.getTitle())
                .bookIsbn(book.getIsbn())
                .borrowDate(transaction.getBorrowDate())
                .dueDate(transaction.getDueDate())
                .returnDate(now)
                .status(BorrowStatus.RETURNED.name())
                .wasOverdue(wasOverdue)
                .message(message)
                .build();
    }

    /**
     * Get borrow history for the authenticated user (paginated).
     */
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getUserBorrowHistory(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return borrowTransactionRepository
                .findByUserIdOrderByBorrowDateDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get active (currently borrowed) books for the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<BorrowResponse> getUserActiveBorrows(String username) {
        User user = userService.findByUsername(username);
        return borrowTransactionRepository
                .findByUserIdAndStatus(user.getId(), BorrowStatus.BORROWED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all overdue books (LIBRARIAN/ADMIN).
     */
    @Transactional(readOnly = true)
    public List<BorrowResponse> getOverdueBooks() {
        return borrowTransactionRepository
                .findOverdueTransactions(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all currently borrowed books (LIBRARIAN/ADMIN).
     */
    @Transactional(readOnly = true)
    public List<BorrowResponse> getAllActiveBorrows() {
        return borrowTransactionRepository
                .findByStatus(BorrowStatus.BORROWED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled task: mark overdue transactions.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void markOverdueTransactions() {
        List<BorrowTransaction> overdueTransactions =
                borrowTransactionRepository.findOverdueTransactions(LocalDateTime.now());

        for (BorrowTransaction transaction : overdueTransactions) {
            if (transaction.getStatus() == BorrowStatus.BORROWED) {
                transaction.setStatus(BorrowStatus.OVERDUE);
                borrowTransactionRepository.save(transaction);
                log.warn("Transaction {} marked as OVERDUE. User: {}, Book: {}",
                        transaction.getTransactionId(),
                        transaction.getUser().getUsername(),
                        transaction.getBook().getTitle());
            }
        }

        if (!overdueTransactions.isEmpty()) {
            log.info("Marked {} transactions as overdue.", overdueTransactions.size());
        }
    }

    /**
     * Map BorrowTransaction entity to BorrowResponse DTO.
     */
    private BorrowResponse mapToResponse(BorrowTransaction transaction) {
        return BorrowResponse.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUser().getId())
                .username(transaction.getUser().getUsername())
                .userFullName(transaction.getUser().getFullName())
                .bookId(transaction.getBook().getId())
                .bookTitle(transaction.getBook().getTitle())
                .bookIsbn(transaction.getBook().getIsbn())
                .bookAuthor(transaction.getBook().getAuthor())
                .borrowDate(transaction.getBorrowDate())
                .dueDate(transaction.getDueDate())
                .returnDate(transaction.getReturnDate())
                .status(transaction.getStatus().name())
                .build();
    }
}
