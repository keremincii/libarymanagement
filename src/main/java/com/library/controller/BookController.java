package com.library.controller;

import com.library.dto.book.BookRequest;
import com.library.dto.book.BookResponse;
import com.library.dto.book.BookSearchCriteria;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for book catalog management.
 * GET endpoints are publicly accessible; write operations require ADMIN or LIBRARIAN role.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "CRUD and search operations for books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns a paginated list of all active books")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search and filter books by multiple criteria")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setTitle(title);
        criteria.setAuthor(author);
        criteria.setIsbn(isbn);
        criteria.setPublisher(publisher);
        criteria.setYearFrom(yearFrom);
        criteria.setYearTo(yearTo);
        criteria.setAvailable(available);

        return ResponseEntity.ok(bookService.searchBooks(criteria, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Returns detailed information about a specific book")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN")
    public ResponseEntity<BookResponse> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a new book", description = "Creates a new book entry (ADMIN/LIBRARIAN only)")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.createBook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a book", description = "Updates book information (ADMIN/LIBRARIAN only)")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a book", description = "Soft-deletes a book (ADMIN only)")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
