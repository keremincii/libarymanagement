package com.library.controller;

import com.library.dto.borrow.BorrowRequest;
import com.library.dto.borrow.BorrowResponse;
import com.library.dto.borrow.ReturnResponse;
import com.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book borrow and return operations.
 */
@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
@Tag(name = "Borrow & Return", description = "Book borrowing and returning operations")
@SecurityRequirement(name = "bearerAuth")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    @Operation(summary = "Borrow a book", description = "Creates a new borrow transaction for the authenticated user")
    public ResponseEntity<BorrowResponse> borrowBook(
            Authentication authentication,
            @Valid @RequestBody BorrowRequest request) {
        BorrowResponse response = borrowService.borrowBook(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/return/{transactionId}")
    @Operation(summary = "Return a book", description = "Returns a borrowed book by transaction ID")
    public ResponseEntity<ReturnResponse> returnBook(
            Authentication authentication,
            @PathVariable Long transactionId) {
        ReturnResponse response = borrowService.returnBook(authentication.getName(), transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get borrow history", description = "Returns paginated borrow history for the authenticated user")
    public ResponseEntity<Page<BorrowResponse>> getBorrowHistory(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                borrowService.getUserBorrowHistory(authentication.getName(), pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active borrows", description = "Returns currently borrowed books for the authenticated user")
    public ResponseEntity<List<BorrowResponse>> getActiveBorrows(Authentication authentication) {
        return ResponseEntity.ok(
                borrowService.getUserActiveBorrows(authentication.getName()));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get overdue books", description = "Returns all overdue books (ADMIN/LIBRARIAN only)")
    public ResponseEntity<List<BorrowResponse>> getOverdueBooks() {
        return ResponseEntity.ok(borrowService.getOverdueBooks());
    }

    @GetMapping("/all-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all active borrows", description = "Returns all currently borrowed books (ADMIN/LIBRARIAN only)")
    public ResponseEntity<List<BorrowResponse>> getAllActiveBorrows() {
        return ResponseEntity.ok(borrowService.getAllActiveBorrows());
    }
}
