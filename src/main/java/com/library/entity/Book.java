package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a book in the library catalog.
 * Initial data is imported from the Kaggle Books Dataset.
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_isbn", columnList = "isbn"),
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_author", columnList = "author")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 300)
    private String author;

    @Column(name = "year_of_publication")
    private Integer yearOfPublication;

    @Column(length = 300)
    private String publisher;

    @Column(name = "image_url_s", length = 500)
    private String imageUrlS;

    @Column(name = "image_url_m", length = 500)
    private String imageUrlM;

    @Column(name = "image_url_l", length = 500)
    private String imageUrlL;

    @Column(name = "total_copies", nullable = false)
    @Builder.Default
    private Integer totalCopies = 3;

    @Column(name = "available_copies", nullable = false)
    @Builder.Default
    private Integer availableCopies = 3;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
