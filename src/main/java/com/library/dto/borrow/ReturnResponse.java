package com.library.dto.borrow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for book return confirmation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnResponse {

    private Long transactionId;
    private String bookTitle;
    private String bookIsbn;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
    private boolean wasOverdue;
    private String message;
}
