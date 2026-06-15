package com.library.service;

import com.library.dto.reservation.ReservationRequest;
import com.library.dto.reservation.ReservationResponse;
import com.library.entity.Book;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.entity.enums.ReservationStatus;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing book reservations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final UserService userService;

    @Value("${app.reservation.expiry-days:3}")
    private int reservationExpiryDays;

    /**
     * Create a reservation for a book that has no available copies.
     */
    @Transactional
    public ReservationResponse createReservation(String username, ReservationRequest request) {
        User user = userService.findByUsername(username);
        Book book = bookService.findBookById(request.getBookId());

        // Validate: book must be active
        if (!book.getActive()) {
            throw new BadRequestException("This book is no longer available in the catalog.");
        }

        // Validate: book must have no available copies (otherwise user should borrow)
        if (book.getAvailableCopies() > 0) {
            throw new BadRequestException(
                    "This book has available copies. You can borrow it directly instead of reserving.");
        }

        // Validate: user doesn't already have a pending reservation for this book
        if (reservationRepository.existsByUserIdAndBookIdAndStatus(
                user.getId(), book.getId(), ReservationStatus.PENDING)) {
            throw new BadRequestException("You already have a pending reservation for this book.");
        }

        // Create reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .reservationDate(LocalDateTime.now())
                .status(ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        // Calculate queue position
        long queuePosition = getQueuePosition(book.getId(), reservation.getReservationId());

        log.info("User '{}' created reservation for book '{}' (ISBN: {}). Queue position: {}",
                username, book.getTitle(), book.getIsbn(), queuePosition);

        return mapToResponse(reservation, queuePosition);
    }

    /**
     * Cancel a reservation.
     */
    @Transactional
    public void cancelReservation(String username, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        User user = userService.findByUsername(username);

        // Validate: reservation belongs to user (or user is ADMIN/LIBRARIAN)
        if (!reservation.getUser().getId().equals(user.getId()) &&
                !user.getRole().name().equals("ADMIN") &&
                !user.getRole().name().equals("LIBRARIAN")) {
            throw new BadRequestException("You can only cancel your own reservations.");
        }

        // Validate: can only cancel PENDING reservations
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Only pending reservations can be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("Reservation {} cancelled by user '{}'", reservationId, username);
    }

    /**
     * Get all reservations for the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(String username) {
        User user = userService.findByUsername(username);
        return reservationRepository.findByUserIdOrderByReservationDateDesc(user.getId())
                .stream()
                .map(r -> mapToResponse(r, getQueuePosition(r.getBook().getId(), r.getReservationId())))
                .collect(Collectors.toList());
    }

    /**
     * Get pending reservations for a specific book (LIBRARIAN/ADMIN).
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getBookReservations(Long bookId) {
        return reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.PENDING)
                .stream()
                .map(r -> mapToResponse(r, getQueuePosition(bookId, r.getReservationId())))
                .collect(Collectors.toList());
    }

    /**
     * Called when a book is returned — fulfills the next pending reservation.
     * Updates reservation status to FULFILLED.
     */
    @Transactional
    public void fulfillNextReservation(Long bookId) {
        Optional<Reservation> nextReservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.PENDING);

        nextReservation.ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.FULFILLED);
            reservationRepository.save(reservation);
            log.info("Reservation {} fulfilled for user '{}', book ID: {}",
                    reservation.getReservationId(),
                    reservation.getUser().getUsername(),
                    bookId);
        });
    }

    /**
     * Scheduled task: expire old pending reservations.
     * Runs every day at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void expireOldReservations() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(reservationExpiryDays);
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(expiryDate);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Reservation {} expired. User: {}, Book: {}",
                    reservation.getReservationId(),
                    reservation.getUser().getUsername(),
                    reservation.getBook().getTitle());
        }

        if (!expiredReservations.isEmpty()) {
            log.info("Expired {} old reservations.", expiredReservations.size());
        }
    }

    /**
     * Calculate queue position for a reservation.
     */
    private long getQueuePosition(Long bookId, Long reservationId) {
        List<Reservation> pendingReservations = reservationRepository
                .findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.PENDING);

        for (int i = 0; i < pendingReservations.size(); i++) {
            if (pendingReservations.get(i).getReservationId().equals(reservationId)) {
                return i + 1;
            }
        }
        return 0; // Not in queue (already fulfilled/cancelled/expired)
    }

    /**
     * Map Reservation entity to ReservationResponse DTO.
     */
    private ReservationResponse mapToResponse(Reservation reservation, long queuePosition) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .userFullName(reservation.getUser().getFullName())
                .bookId(reservation.getBook().getId())
                .bookTitle(reservation.getBook().getTitle())
                .bookIsbn(reservation.getBook().getIsbn())
                .bookAuthor(reservation.getBook().getAuthor())
                .reservationDate(reservation.getReservationDate())
                .status(reservation.getStatus().name())
                .queuePosition(queuePosition)
                .build();
    }
}
