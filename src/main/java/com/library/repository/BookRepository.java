package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Book entity operations.
 * Extends JpaSpecificationExecutor for dynamic search/filter queries.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    /**
     * Search books by title, author, isbn, or publisher (case-insensitive).
     */
    @Query("SELECT b FROM Book b WHERE b.active = true AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    Page<Book> findByActiveTrue(Pageable pageable);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.active = true")
    long countActiveBooks();

    @Query("SELECT COUNT(b) FROM Book b WHERE b.active = true AND b.availableCopies > 0")
    long countAvailableBooks();
}
