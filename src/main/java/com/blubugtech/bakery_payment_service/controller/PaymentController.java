package com.blubugtech.bakery_payment_service.controller;

import com.blubugtech.bakery_payment_service.enums.PaymentStatus;

import com.blubugtech.bakery_payment_service.dto.payment.*;
import com.blubugtech.bakery_payment_service.dto.refund.*;
import com.blubugtech.bakery_payment_service.dto.transaction.*;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Payment Management APIs")

public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    final private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create payment
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Create payment request received for order: {}", request.getOrderId());

        // Use header userId if available (from Gateway), otherwise use request userId
        if (userId != null) {
            request.setUserId(userId);
        }

        PaymentResponse payment = paymentService.createPayment(request);

        logger.info("Payment created successfully: {}", payment.getPaymentReference());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    // Get all payments with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get all payments request received (page: {}, size: {})", page, size);

        // Only admins can view all payments
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);

        logger.info("Retrieved {} payments (page {} of {})", payments.getContent().size(),
                   page + 1, payments.getTotalPages());
        return ResponseEntity.ok(payments);
    }

    // Get payment by ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get payment by ID request received: {}", paymentId);

        PaymentResponse payment = paymentService.getPaymentById(paymentId);

        // Check if user can access this payment (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !payment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Payment retrieved: {}", payment.getPaymentReference());
        return ResponseEntity.ok(payment);
    }

    // Get payment by reference
    @GetMapping("/reference/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(
            @PathVariable String paymentReference,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get payment by reference request received: {}", paymentReference);

        PaymentResponse payment = paymentService.getPaymentByReference(paymentReference);

        // Check if user can access this payment (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !payment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Payment found: {}", paymentReference);
        return ResponseEntity.ok(payment);
    }

    // Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get payment by order ID request received: {}", orderId);

        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);

        // Check if user can access this payment (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !payment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Payment found for order: {}", orderId);
        return ResponseEntity.ok(payment);
    }

    // Get payments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get payments by user ID request received: {}", userId);

        // Check if user can access these payments (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);

        logger.info("Retrieved {} payments for user", payments.size());
        return ResponseEntity.ok(payments);
    }

    // Get payments by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get payments by status request received: {}", status);

        // Only admins can view payments by status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);

        logger.info("Retrieved {} payments with status {}", payments.size(), status);
        return ResponseEntity.ok(payments);
    }

    // Update payment status
    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Update payment status request received: {} to {}", paymentId, request.getStatus());

        // Only admins can update payment status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PaymentResponse payment = paymentService.updatePaymentStatus(paymentId, request);

        logger.info("Payment status updated successfully: {}", paymentId);
        return ResponseEntity.ok(payment);
    }

    // Cancel payment
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable UUID paymentId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Cancel payment request received: {}", paymentId);

        // Check if user can cancel this payment
        if (userId != null && !"ADMIN".equals(userRole)) {
            PaymentResponse existingPayment = paymentService.getPaymentById(paymentId);
            if (!existingPayment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String reason = request.get("reason");
        PaymentResponse payment = paymentService.cancelPayment(paymentId, reason);

        logger.info("Payment cancelled successfully: {}", paymentId);
        return ResponseEntity.ok(payment);
    }

    // Retry payment
    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Retry payment request received: {}", paymentId);

        // Check if user can retry this payment
        if (userId != null && !"ADMIN".equals(userRole)) {
            PaymentResponse existingPayment = paymentService.getPaymentById(paymentId);
            if (!existingPayment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        PaymentResponse payment = paymentService.retryPayment(paymentId);

        logger.info("Payment retry initiated: {}", paymentId);
        return ResponseEntity.ok(payment);
    }

    // Send OTP
    @PostMapping("/{paymentId}/send-otp")
    public ResponseEntity<Void> sendOtp(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Send OTP request received: {}", paymentId);

        if (userId != null && !"ADMIN".equals(userRole)) {
            PaymentResponse existingPayment = paymentService.getPaymentById(paymentId);
            if (!existingPayment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        paymentService.sendOtp(paymentId);
        return ResponseEntity.ok().build();
    }

    // Verify OTP
    @PostMapping("/{paymentId}/verify-otp")
    public ResponseEntity<PaymentResponse> verifyOtp(
            @PathVariable UUID paymentId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Verify OTP request received: {}", paymentId);

        if (userId != null && !"ADMIN".equals(userRole)) {
            PaymentResponse existingPayment = paymentService.getPaymentById(paymentId);
            if (!existingPayment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String otp = request.get("otp");
        PaymentResponse payment = paymentService.verifyOtp(paymentId, otp);
        return ResponseEntity.ok(payment);
    }

    // Get payment statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get payment statistics request received");

        // Only admins can view statistics
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = paymentService.getPaymentStatistics(startDate, endDate);

        logger.info("Payment statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

}
