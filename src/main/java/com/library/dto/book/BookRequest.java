package com.library.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a book.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must be at most 20 characters")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be at most 500 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 300, message = "Author must be at most 300 characters")
    private String author;

    private Integer yearOfPublication;

    @Size(max = 300, message = "Publisher must be at most 300 characters")
    private String publisher;

    private String imageUrlS;
    private String imageUrlM;
    private String imageUrlL;

    private Integer totalCopies = 3;
}
