package com.blubugtech.bakery_payment_service.controller;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentRequest;
import com.blubugtech.bakery_payment_service.dto.payment.PaymentResponse;
import com.blubugtech.bakery_payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Payment Management APIs")

@Slf4j
public class PaymentController {

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

        log.info("Create payment request received for order: {}", request.getOrderId());

        // Use header userId if available (from Gateway), otherwise use request userId
        if (userId != null) {
            request.setUserId(userId);
        }

        PaymentResponse payment = paymentService.createPayment(request);

        log.info("Payment created successfully: {}", payment.getPaymentReference());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }


    // Get payment by ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get payment by ID request received: {}", paymentId);

        PaymentResponse payment = paymentService.getPaymentById(paymentId);

        // Check if user can access this payment (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !payment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Payment retrieved: {}", payment.getPaymentReference());
        return ResponseEntity.ok(payment);
    }

    // Get payment by reference
    @GetMapping("/reference/{paymentReference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(
            @PathVariable String paymentReference,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get payment by reference request received: {}", paymentReference);

        PaymentResponse payment = paymentService.getPaymentByReference(paymentReference);

        // Check if user can access this payment (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !payment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Payment found: {}", paymentReference);
        return ResponseEntity.ok(payment);
    }

    // Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get payment by order ID request received: {}", orderId);

        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);

        // Check if user can access this payment (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !payment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Payment found for order: {}", orderId);
        return ResponseEntity.ok(payment);
    }

    // Get payments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedModel<PaymentResponse>> getPaymentsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get payments by user ID request received: {}", userId);

        // Check if user can access these payments (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedModel<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId, pageable);

        log.info("Retrieved {} payments for user", payments.getContent().size());
        return ResponseEntity.ok(payments);
    }


    // Cancel payment
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable UUID paymentId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Cancel payment request received: {}", paymentId);

        // Check if user can cancel this payment
        if (userId != null && !"ADMIN".equals(userRole)) {
            PaymentResponse existingPayment = paymentService.getPaymentById(paymentId);
            if (!existingPayment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String reason = request.get("reason");
        PaymentResponse payment = paymentService.cancelPayment(paymentId, reason);

        log.info("Payment cancelled successfully: {}", paymentId);
        return ResponseEntity.ok(payment);
    }

    // Retry payment
    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Retry payment request received: {}", paymentId);

        // Check if user can retry this payment
        if (userId != null && !"ADMIN".equals(userRole)) {
            PaymentResponse existingPayment = paymentService.getPaymentById(paymentId);
            if (!existingPayment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        PaymentResponse payment = paymentService.retryPayment(paymentId);

        log.info("Payment retry initiated: {}", paymentId);
        return ResponseEntity.ok(payment);
    }

    // Send OTP
    @PostMapping("/{paymentId}/send-otp")
    public ResponseEntity<Void> sendOtp(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Send OTP request received: {}", paymentId);

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

        log.info("Verify OTP request received: {}", paymentId);

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


}
