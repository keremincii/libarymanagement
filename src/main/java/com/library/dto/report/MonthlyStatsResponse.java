package com.library.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for monthly borrow statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatsResponse {

    private int year;
    private int month;
    private String monthName;
    private Long borrowCount;
}
