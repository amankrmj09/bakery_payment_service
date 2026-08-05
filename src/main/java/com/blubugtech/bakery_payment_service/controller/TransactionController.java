package com.blubugtech.bakery_payment_service.controller;

import com.blubugtech.bakery_payment_service.dto.transaction.PaymentTransactionResponse;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import com.blubugtech.bakery_payment_service.service.PaymentTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")

@Slf4j
public class TransactionController {


    final private PaymentTransactionService paymentTransactionService;

    public TransactionController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    // Get transaction by ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentTransactionResponse> getTransactionById(
            @PathVariable UUID transactionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get transaction by ID request received: {}", transactionId);

        PaymentTransactionResponse transaction = paymentTransactionService.getTransactionById(transactionId);

        log.info("Transaction retrieved: {}", transactionId);
        return ResponseEntity.ok(transaction);
    }

    // Get transactions by payment ID
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<org.springframework.data.web.PagedModel<PaymentTransactionResponse>> getTransactionsByPaymentId(
            @PathVariable UUID paymentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get transactions by payment ID request received: {}", paymentId);

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<PaymentTransactionResponse> transactions = paymentTransactionService.getTransactionsByPaymentId(paymentId, pageable);

        log.info("Retrieved transactions for payment");
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.web.PagedModel<PaymentTransactionResponse>> getTransactionsByStatus(
            @PathVariable TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get transactions by status request received: {}", status);

        // Only admins can view transactions by status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<PaymentTransactionResponse> transactions = paymentTransactionService.getTransactionsByStatus(status, pageable);

        log.info("Retrieved transactions with status {}", status);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by type
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.web.PagedModel<PaymentTransactionResponse>> getTransactionsByType(
            @PathVariable TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get transactions by type request received: {}", type);

        // Only admins can view transactions by type
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<PaymentTransactionResponse> transactions = paymentTransactionService.getTransactionsByType(type, pageable);

        log.info("Retrieved transactions with type {}", type);
        return ResponseEntity.ok(transactions);
    }

    // Get pending transactions
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.web.PagedModel<PaymentTransactionResponse>> getPendingTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get pending transactions request received");

        // Only admins can view pending transactions
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<PaymentTransactionResponse> transactions = paymentTransactionService.getPendingTransactions(pageable);

        log.info("Retrieved pending transactions");
        return ResponseEntity.ok(transactions);
    }

    // Get failed transactions
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.web.PagedModel<PaymentTransactionResponse>> getFailedTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get failed transactions request received");

        // Only admins can view failed transactions
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<PaymentTransactionResponse> transactions = paymentTransactionService.getFailedTransactions(pageable);

        log.info("Retrieved failed transactions");
        return ResponseEntity.ok(transactions);
    }

    // Get transaction statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get transaction statistics request received");

        // Only admins can view statistics
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = paymentTransactionService.getTransactionStatistics(startDate, endDate);

        log.info("Transaction statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

}
