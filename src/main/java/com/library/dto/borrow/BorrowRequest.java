package com.library.dto.borrow;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for borrowing a book.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;
}
