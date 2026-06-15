package com.library.repository;

import com.library.dto.book.BookSearchCriteria;
import com.library.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for dynamic book filtering.
 * Builds query predicates based on the provided search criteria.
 */
public class BookSpecification {

    private BookSpecification() {
        // Utility class — prevent instantiation
    }

    /**
     * Build a Specification from search criteria.
     */
    public static Specification<Book> withCriteria(BookSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter only active books
            predicates.add(cb.isTrue(root.get("active")));

            // General keyword search (across multiple fields)
            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate authorMatch = cb.like(cb.lower(root.get("author")), pattern);
                Predicate isbnMatch = cb.like(cb.lower(root.get("isbn")), pattern);
                Predicate publisherMatch = cb.like(cb.lower(root.get("publisher")), pattern);
                predicates.add(cb.or(titleMatch, authorMatch, isbnMatch, publisherMatch));
            }

            // Specific field filters
            if (criteria.getTitle() != null && !criteria.getTitle().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + criteria.getTitle().toLowerCase() + "%"));
            }

            if (criteria.getAuthor() != null && !criteria.getAuthor().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("author")),
                        "%" + criteria.getAuthor().toLowerCase() + "%"));
            }

            if (criteria.getIsbn() != null && !criteria.getIsbn().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("isbn")),
                        "%" + criteria.getIsbn().toLowerCase() + "%"));
            }

            if (criteria.getPublisher() != null && !criteria.getPublisher().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("publisher")),
                        "%" + criteria.getPublisher().toLowerCase() + "%"));
            }

            // Year range filter
            if (criteria.getYearFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearOfPublication"),
                        criteria.getYearFrom()));
            }

            if (criteria.getYearTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("yearOfPublication"),
                        criteria.getYearTo()));
            }

            // Availability filter
            if (criteria.getAvailable() != null && criteria.getAvailable()) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
