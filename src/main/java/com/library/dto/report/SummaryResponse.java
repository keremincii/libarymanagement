package com.library.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for dashboard summary statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryResponse {

    private long totalBooks;
    private long availableBooks;
    private long totalUsers;
    private long activeBorrows;
    private long overdueBooks;
    private long pendingReservations;
}
