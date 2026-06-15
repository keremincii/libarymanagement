package com.library.service;

import com.library.dto.book.BookRequest;
import com.library.dto.book.BookResponse;
import com.library.dto.book.BookSearchCriteria;
import com.library.entity.Book;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BookSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for book catalog management (CRUD + search/filter).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    /**
     * Get all active books with pagination.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search books with dynamic criteria and pagination.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        return bookRepository.findAll(BookSpecification.withCriteria(criteria), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get a single book by ID.
     */
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = findBookById(id);
        return mapToResponse(book);
    }

    /**
     * Get a single book by ISBN.
     */
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "isbn", isbn));
        return mapToResponse(book);
    }

    /**
     * Create a new book.
     */
    @Transactional
    public BookResponse createBook(BookRequest request) {
        // Check for duplicate ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("Book", "isbn", request.getIsbn());
        }

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .yearOfPublication(request.getYearOfPublication())
                .publisher(request.getPublisher())
                .imageUrlS(request.getImageUrlS())
                .imageUrlM(request.getImageUrlM())
                .imageUrlL(request.getImageUrlL())
                .totalCopies(request.getTotalCopies() != null ? request.getTotalCopies() : 3)
                .availableCopies(request.getTotalCopies() != null ? request.getTotalCopies() : 3)
                .active(true)
                .build();

        book = bookRepository.save(book);
        log.info("New book created: {} (ISBN: {})", book.getTitle(), book.getIsbn());

        return mapToResponse(book);
    }

    /**
     * Update an existing book.
     */
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookById(id);

        // Check if ISBN is being changed to one that already exists
        if (!book.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("Book", "isbn", request.getIsbn());
        }

        // Calculate difference in total copies to adjust available copies
        int copyDifference = (request.getTotalCopies() != null ? request.getTotalCopies() : book.getTotalCopies())
                - book.getTotalCopies();

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setYearOfPublication(request.getYearOfPublication());
        book.setPublisher(request.getPublisher());
        book.setImageUrlS(request.getImageUrlS());
        book.setImageUrlM(request.getImageUrlM());
        book.setImageUrlL(request.getImageUrlL());

        if (request.getTotalCopies() != null) {
            book.setTotalCopies(request.getTotalCopies());
            book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + copyDifference));
        }

        book = bookRepository.save(book);
        log.info("Book updated: {} (ISBN: {})", book.getTitle(), book.getIsbn());

        return mapToResponse(book);
    }

    /**
     * Soft-delete a book (set active = false).
     */
    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookById(id);
        book.setActive(false);
        bookRepository.save(book);
        log.info("Book deactivated: {} (ISBN: {})", book.getTitle(), book.getIsbn());
    }

    /**
     * Find a Book entity by ID (internal use).
     */
    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }

    /**
     * Map Book entity to BookResponse DTO.
     */
    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .yearOfPublication(book.getYearOfPublication())
                .publisher(book.getPublisher())
                .imageUrlS(book.getImageUrlS())
                .imageUrlM(book.getImageUrlM())
                .imageUrlL(book.getImageUrlL())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .active(book.getActive())
                .createdAt(book.getCreatedAt())
                .build();
    }
}
