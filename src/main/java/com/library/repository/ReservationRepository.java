package com.library.repository;

import com.library.entity.Reservation;
import com.library.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Reservation entity operations.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find all reservations for a specific user.
     */
    List<Reservation> findByUserIdOrderByReservationDateDesc(Long userId);

    /**
     * Find pending reservations for a specific book (ordered by date — first come first served).
     */
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Long bookId, ReservationStatus status);

    /**
     * Check if user already has a pending reservation for this book.
     */
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, ReservationStatus status);

    /**
     * Find the oldest pending reservation for a book (next in queue).
     */
    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservationDateAsc(Long bookId, ReservationStatus status);

    /**
     * Find expired pending reservations (older than expiry threshold).
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.reservationDate < :expiryDate")
    List<Reservation> findExpiredReservations(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Count pending reservations for a book.
     */
    long countByBookIdAndStatus(Long bookId, ReservationStatus status);
}
