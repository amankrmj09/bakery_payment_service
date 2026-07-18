package com.blubugtech.bakery_payment_service.controller;

import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;

import com.blubugtech.bakery_payment_service.dto.transaction.*;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.service.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")

public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);


    final private PaymentTransactionService paymentTransactionService;

    public TransactionController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    // Get transaction by ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentTransactionResponse> getTransactionById(
            @PathVariable UUID transactionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get transaction by ID request received: {}", transactionId);

        PaymentTransactionResponse transaction = paymentTransactionService.getTransactionById(transactionId);

        logger.info("Transaction retrieved: {}", transactionId);
        return ResponseEntity.ok(transaction);
    }

    // Get transactions by payment ID
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<PaymentTransactionResponse>> getTransactionsByPaymentId(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get transactions by payment ID request received: {}", paymentId);

        List<PaymentTransactionResponse> transactions = paymentTransactionService.getTransactionsByPaymentId(paymentId);

        logger.info("Retrieved {} transactions for payment", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentTransactionResponse>> getTransactionsByStatus(
            @PathVariable TransactionStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get transactions by status request received: {}", status);

        // Only admins can view transactions by status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        List<PaymentTransactionResponse> transactions = paymentTransactionService.getTransactionsByStatus(status);

        logger.info("Retrieved {} transactions with status {}", transactions.size(), status);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by type
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentTransactionResponse>> getTransactionsByType(
            @PathVariable TransactionType type,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get transactions by type request received: {}", type);

        // Only admins can view transactions by type
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        List<PaymentTransactionResponse> transactions = paymentTransactionService.getTransactionsByType(type);

        logger.info("Retrieved {} transactions with type {}", transactions.size(), type);
        return ResponseEntity.ok(transactions);
    }

    // Get pending transactions
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentTransactionResponse>> getPendingTransactions(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get pending transactions request received");

        // Only admins can view pending transactions
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        List<PaymentTransactionResponse> transactions = paymentTransactionService.getPendingTransactions();

        logger.info("Retrieved {} pending transactions", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    // Get failed transactions
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentTransactionResponse>> getFailedTransactions(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get failed transactions request received");

        // Only admins can view failed transactions
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        List<PaymentTransactionResponse> transactions = paymentTransactionService.getFailedTransactions();

        logger.info("Retrieved {} failed transactions", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    // Get transaction statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get transaction statistics request received");

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

        logger.info("Transaction statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

}
