package com.library.dto.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for book search/filter criteria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchCriteria {

    private String keyword;      // General search across title, author, isbn, publisher
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Integer yearFrom;
    private Integer yearTo;
    private Boolean available;   // true = only books with availableCopies > 0
}
