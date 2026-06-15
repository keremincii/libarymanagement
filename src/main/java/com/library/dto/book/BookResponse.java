package com.library.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for book information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Integer yearOfPublication;
    private String publisher;
    private String imageUrlS;
    private String imageUrlM;
    private String imageUrlL;
    private Integer totalCopies;
    private Integer availableCopies;
    private Boolean active;
    private LocalDateTime createdAt;
}
