package com.library.controller;

import com.library.dto.reservation.ReservationRequest;
import com.library.dto.reservation.ReservationResponse;
import com.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book reservation operations.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Book reservation management")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a reservation",
               description = "Reserve a book that has no available copies")
    public ResponseEntity<ReservationResponse> createReservation(
            Authentication authentication,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService
                .createReservation(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get my reservations",
               description = "Returns all reservations for the authenticated user")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            Authentication authentication) {
        return ResponseEntity.ok(
                reservationService.getUserReservations(authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation",
               description = "Cancel a pending reservation")
    public ResponseEntity<Void> cancelReservation(
            Authentication authentication,
            @PathVariable Long id) {
        reservationService.cancelReservation(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get reservations for a book",
               description = "Returns pending reservations for a specific book (ADMIN/LIBRARIAN only)")
    public ResponseEntity<List<ReservationResponse>> getBookReservations(
            @PathVariable Long bookId) {
        return ResponseEntity.ok(reservationService.getBookReservations(bookId));
    }
}
