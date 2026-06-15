package com.library.dto.borrow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for borrow transaction details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowResponse {

    private Long transactionId;
    private Long userId;
    private String username;
    private String userFullName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private String bookAuthor;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
}
