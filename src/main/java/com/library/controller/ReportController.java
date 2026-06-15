package com.library.controller;

import com.library.dto.report.ActiveUserResponse;
import com.library.dto.report.MonthlyStatsResponse;
import com.library.dto.report.MostBorrowedBookResponse;
import com.library.dto.report.SummaryResponse;
import com.library.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reports and analytics.
 * All endpoints require ADMIN or LIBRARIAN role.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
@Tag(name = "Reports & Analytics", description = "Library reports and statistics")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Dashboard summary",
               description = "Returns key metrics: total books, available books, users, active borrows, overdue, reservations")
    public ResponseEntity<SummaryResponse> getSummary() {
        return ResponseEntity.ok(reportService.getSummary());
    }

    @GetMapping("/most-borrowed")
    @Operation(summary = "Most borrowed books",
               description = "Returns the top N most borrowed books")
    public ResponseEntity<List<MostBorrowedBookResponse>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getMostBorrowedBooks(limit));
    }

    @GetMapping("/active-users")
    @Operation(summary = "Most active users",
               description = "Returns the top N users by borrow count")
    public ResponseEntity<List<ActiveUserResponse>> getMostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getMostActiveUsers(limit));
    }

    @GetMapping("/monthly-stats")
    @Operation(summary = "Monthly borrow statistics",
               description = "Returns monthly borrow counts for the last N months")
    public ResponseEntity<List<MonthlyStatsResponse>> getMonthlyStats(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(reportService.getMonthlyStats(months));
    }
}
