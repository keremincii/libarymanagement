package com.library.service;

import com.library.dto.report.ActiveUserResponse;
import com.library.dto.report.MonthlyStatsResponse;
import com.library.dto.report.MostBorrowedBookResponse;
import com.library.dto.report.SummaryResponse;
import com.library.entity.enums.BorrowStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.repository.BookRepository;
import com.library.repository.BorrowTransactionRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service for generating reports and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Get dashboard summary statistics.
     */
    @Transactional(readOnly = true)
    public SummaryResponse getSummary() {
        long totalBooks = bookRepository.countActiveBooks();
        long availableBooks = bookRepository.countAvailableBooks();
        long totalUsers = userRepository.count();
        long activeBorrows = borrowTransactionRepository.findByStatus(BorrowStatus.BORROWED).size();
        long overdueBooks = borrowTransactionRepository.findOverdueTransactions(LocalDateTime.now()).size();
        long pendingReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .count();

        return SummaryResponse.builder()
                .totalBooks(totalBooks)
                .availableBooks(availableBooks)
                .totalUsers(totalUsers)
                .activeBorrows(activeBorrows)
                .overdueBooks(overdueBooks)
                .pendingReservations(pendingReservations)
                .build();
    }

    /**
     * Get the most borrowed books.
     */
    @Transactional(readOnly = true)
    public List<MostBorrowedBookResponse> getMostBorrowedBooks(int limit) {
        List<Object[]> results = borrowTransactionRepository
                .findMostBorrowedBooks(PageRequest.of(0, limit));

        return results.stream()
                .map(row -> MostBorrowedBookResponse.builder()
                        .bookId((Long) row[0])
                        .title((String) row[1])
                        .author((String) row[2])
                        .borrowCount((Long) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get the most active users by borrow count.
     */
    @Transactional(readOnly = true)
    public List<ActiveUserResponse> getMostActiveUsers(int limit) {
        List<Object[]> results = borrowTransactionRepository
                .findMostActiveUsers(PageRequest.of(0, limit));

        return results.stream()
                .map(row -> ActiveUserResponse.builder()
                        .userId((Long) row[0])
                        .fullName((String) row[1])
                        .username((String) row[2])
                        .borrowCount((Long) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get monthly borrow statistics for the last N months.
     */
    @Transactional(readOnly = true)
    public List<MonthlyStatsResponse> getMonthlyStats(int months) {
        List<MonthlyStatsResponse> stats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime startOfMonth = now.minusMonths(i).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            long borrowCount = borrowTransactionRepository
                    .countByBorrowDateBetween(startOfMonth, endOfMonth);

            Month month = startOfMonth.getMonth();
            String monthName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            stats.add(MonthlyStatsResponse.builder()
                    .year(startOfMonth.getYear())
                    .month(month.getValue())
                    .monthName(monthName)
                    .borrowCount(borrowCount)
                    .build());
        }

        return stats;
    }
}
