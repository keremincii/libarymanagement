package com.library.repository;

import com.library.entity.BorrowTransaction;
import com.library.entity.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BorrowTransaction entity operations.
 */
@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    /**
     * Find all transactions for a specific user.
     */
    Page<BorrowTransaction> findByUserIdOrderByBorrowDateDesc(Long userId, Pageable pageable);

    /**
     * Find active (currently borrowed) transactions for a user.
     */
    List<BorrowTransaction> findByUserIdAndStatus(Long userId, BorrowStatus status);

    /**
     * Count how many books a user currently has borrowed.
     */
    @Query("SELECT COUNT(bt) FROM BorrowTransaction bt WHERE bt.user.id = :userId AND bt.status = 'BORROWED'")
    long countActiveBorrowsByUser(@Param("userId") Long userId);

    /**
     * Check if a user has already borrowed a specific book (and hasn't returned it).
     */
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowStatus status);

    /**
     * Find all overdue transactions (due date passed but not returned).
     */
    @Query("SELECT bt FROM BorrowTransaction bt WHERE bt.status = 'BORROWED' AND bt.dueDate < :now")
    List<BorrowTransaction> findOverdueTransactions(@Param("now") LocalDateTime now);

    /**
     * Find all currently borrowed transactions.
     */
    List<BorrowTransaction> findByStatus(BorrowStatus status);

    /**
     * Count transactions within a date range (for monthly stats).
     */
    @Query("SELECT COUNT(bt) FROM BorrowTransaction bt WHERE bt.borrowDate BETWEEN :startDate AND :endDate")
    long countByBorrowDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find most borrowed books (for reporting).
     */
    @Query("SELECT bt.book.id, bt.book.title, bt.book.author, COUNT(bt) as borrowCount " +
           "FROM BorrowTransaction bt " +
           "GROUP BY bt.book.id, bt.book.title, bt.book.author " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedBooks(Pageable pageable);

    /**
     * Find most active users (for reporting).
     */
    @Query("SELECT bt.user.id, bt.user.fullName, bt.user.username, COUNT(bt) as borrowCount " +
           "FROM BorrowTransaction bt " +
           "GROUP BY bt.user.id, bt.user.fullName, bt.user.username " +
           "ORDER BY borrowCount DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);
}
