package com.library.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for most borrowed books report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostBorrowedBookResponse {

    private Long bookId;
    private String title;
    private String author;
    private Long borrowCount;
}
