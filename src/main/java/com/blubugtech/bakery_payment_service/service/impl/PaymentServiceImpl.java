package com.blubugtech.bakery_payment_service.service.impl;

import com.blubugtech.bakery_payment_service.client.OrderClient;
import com.blubugtech.bakery_payment_service.client.UserClient;
import com.blubugtech.bakery_payment_service.dto.payment.PaymentRequest;
import com.blubugtech.bakery_payment_service.dto.payment.PaymentResponse;
import com.blubugtech.bakery_payment_service.dto.payment.PaymentStatusUpdateRequest;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.enums.PaymentMethod;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.exception.payment.InvalidPaymentAmountException;
import com.blubugtech.bakery_payment_service.exception.payment.InvalidPaymentStatusException;
import com.blubugtech.bakery_payment_service.exception.payment.PaymentNotFoundException;
import com.blubugtech.bakery_payment_service.exception.payment.PaymentServiceException;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGateway;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGatewayResult;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.service.*;
import com.blubugtech.bakery_payment_service.mapper.PaymentMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    final private PaymentRepository paymentRepository;

    final private PaymentTransactionService paymentTransactionService;

    final private RefundService refundService;

    final private List<PaymentGateway> paymentGateways;


    final private ObjectMapper objectMapper;
    final private UserClient userClient;
    final private PaymentProcessingService paymentProcessingService;

    final private ApplicationEventPublisher applicationEventPublisher;
    final private OtpService otpService;

    final private OrderClient orderClient;

    final private KafkaTemplate<String, Object> kafkaTemplate;

    final private PaymentMapper paymentMapper;

    @Value("${payment.limits.min-amount:0.50}")
    private BigDecimal minPaymentAmount;

    @Value("${payment.limits.max-amount:10000.00}")
    private BigDecimal maxPaymentAmount;

    @Value("${payment.limits.daily-limit:50000.00}")
    private BigDecimal dailyPaymentLimit;

    // Create payment
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order: {} amount: {}", request.getOrderId(), request.getAmount());

        try {
            // Validate payment request
            validatePaymentRequest(request);

            // Check if payment already exists for order
            Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
            if (existingPayment.isPresent()) {
                throw new PaymentServiceException("Payment already exists for order: " + request.getOrderId());
            }

            // Order validation skipped because this is triggered by OrderEvent

            // Create payment entity
            Payment payment = new Payment(request.getOrderId(), request.getUserId(),
                    request.getPaymentMethod(), request.getAmount(),
                    request.getDescription());

            payment.setPaymentGateway(request.getPaymentGateway());
            payment.setCurrencyCode(request.getCurrencyCode());
            payment.setCardLastFour(request.getCardLastFour());
            payment.setCardBrand(request.getCardBrand());
            payment.setCardType(request.getCardType());
            payment.setDigitalWalletProvider(request.getDigitalWalletProvider());
            payment.setBankName(request.getBankName());
            payment.setExternalTransactionId(request.getExternalTransactionId());
            payment.setNotes(request.getNotes());

            // Set metadata
            if (request.getMetadata() != null) {
                payment.setMetadata(convertMetadataToJson(request.getMetadata()));
            }

            // Set expiration (15 minutes from now for non-cash payments)
            if (request.getPaymentMethod() != PaymentMethod.CASH) {
                payment.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            }

            // Payment is kept in PENDING until OTP is verified
            payment.setStatus(PaymentStatus.PENDING);

            // Save payment
            Payment savedPayment = paymentRepository.save(payment);

            log.info("Payment created successfully (PENDING for OTP): {}", savedPayment.getPaymentReference());
            return paymentMapper.toResponse(savedPayment);

        } catch (Exception e) {
            log.error("Failed to create payment for order {}: {}", request.getOrderId(), e.getMessage(), e);
            throw new PaymentServiceException("Failed to create payment: " + e.getMessage());
        }
    }

    // Get payment by ID
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        log.debug("Fetching payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        return paymentMapper.toResponse(payment);
    }

    // Get payment by reference
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String paymentReference) {
        log.debug("Fetching payment by reference: {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with reference: " + paymentReference));

        return paymentMapper.toResponse(payment);
    }

    // Get payment by order ID
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        log.debug("Fetching payment by order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));

        return paymentMapper.toResponse(payment);
    }

    // Get payments by user ID
    @Transactional(readOnly = true)
    public PagedModel<PaymentResponse> getPaymentsByUserId(UUID userId, Pageable pageable) {
        log.debug("Fetching payments for user: {}", userId);

        Page<PaymentResponse> paymentPage = paymentRepository.findByUserId(userId, pageable)
                .map(paymentMapper::toResponse);
        return new PagedModel<>(paymentPage);
    }

    // Get payments by status
    @Transactional(readOnly = true)
    public PagedModel<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Fetching payments by status: {}", status);

        Page<PaymentResponse> paymentPage = paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(paymentMapper::toResponse);
        return new PagedModel<>(paymentPage);
    }

    // Get all payments with pagination
    @Transactional(readOnly = true)
    public PagedModel<PaymentResponse> getAllPayments(Pageable pageable) {
        log.debug("Fetching all payments with pagination");

        Page<PaymentResponse> paymentPage = paymentRepository.findAll(pageable)
                .map(paymentMapper::toResponse);
        return new PagedModel<>(paymentPage);
    }

    // Update payment status
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatusUpdateRequest request) {
        log.info("Updating payment status: {} to {}", paymentId, request.getStatus());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        // Validate status transition
        validateStatusTransition(payment.getStatus(), request.getStatus());

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(request.getStatus());

        // Handle status-specific logic
        handleStatusTransition(payment, oldStatus, request.getStatus(), request.getReason());

        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }

        if (request.getGatewayResponse() != null) {
            payment.setGatewayResponse(request.getGatewayResponse());
        }

        Payment updatedPayment = paymentRepository.save(payment);

        // Notify order service of payment status change
        applicationEventPublisher.publishEvent(new com.blubugtech.bakery_payment_service.event.PaymentStatusUpdatedApplicationEvent(this, updatedPayment));

        log.info("Payment status updated successfully: {} from {} to {}",
                paymentId, oldStatus, request.getStatus());

        return paymentMapper.toResponse(updatedPayment);
    }

    // Cancel payment
    public PaymentResponse cancelPayment(UUID paymentId, String reason) {
        log.info("Cancelling payment: {} with reason: {}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentServiceException("Cannot cancel completed payment. Use refund instead.");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new PaymentServiceException("Payment is already cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        payment.setFailureReason(reason);

        // Try to void the payment at gateway if it was authorized
        if (payment.getAuthorizedAt() != null) {
            try {
                PaymentGatewayResult voidResponse = com.blubugtech.bakery_payment_service.integration.payment.PaymentGatewayResult.builder().success(true).pending(false).gatewayTransactionId(payment.getGatewayPaymentId()).gatewayResponse("Voided").rawResponse("{}").gatewayFee(java.math.BigDecimal.ZERO).build();
                payment.setGatewayResponse(voidResponse.getGatewayResponse());
                payment.setGatewayRawResponse(voidResponse.getRawResponse());
            } catch (Exception e) {
                log.warn("Failed to void payment at gateway: {}", e.getMessage(), e);
            }
        }

        Payment cancelledPayment = paymentRepository.save(payment);

        // Notify order service
        applicationEventPublisher.publishEvent(new com.blubugtech.bakery_payment_service.event.PaymentStatusUpdatedApplicationEvent(this, cancelledPayment));

        log.info("Payment cancelled successfully: {}", paymentId);
        return paymentMapper.toResponse(cancelledPayment);
    }

    // Retry failed payment
    public PaymentResponse retryPayment(UUID paymentId) {
        log.info("Retrying failed payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (!payment.canBeRetried()) {
            throw new PaymentServiceException("Payment cannot be retried");
        }

        payment.incrementRetryCount();
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setFailureReason(null);
        payment.setFailureCode(null);

        Payment savedPayment = paymentRepository.save(payment);

        // Process payment asynchronously
        paymentProcessingService.processPaymentAsync(savedPayment);

        log.info("Payment retry initiated: {}", paymentId);
        return paymentMapper.toResponse(savedPayment);
    }

    // Get payment statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching payment statistics");

        try {
            Object[] successRate = paymentRepository.getPaymentSuccessRate(startDate, endDate);
            List<Object[]> methodStats = paymentRepository.getPaymentStatisticsByMethod(startDate, endDate);
            List<Object[]> gatewayStats = paymentRepository.getPaymentStatisticsByGateway(startDate, endDate);
            List<Object[]> statusStats = paymentRepository.getPaymentStatisticsByStatus(startDate, endDate);
            BigDecimal totalAmount = paymentRepository.getTotalAmountByDateRange(startDate, endDate);
            BigDecimal totalFees = paymentRepository.getTotalGatewayFees(startDate, endDate);
            BigDecimal netAmount = paymentRepository.getTotalNetAmount(startDate, endDate);

            return Map.ofEntries(
                    Map.entry("totalPayments", successRate[0]),
                    Map.entry("successfulPayments", successRate[1]),
                    Map.entry("failedPayments", successRate[2]),
                    Map.entry("pendingPayments", successRate[3]),
                    Map.entry("totalAmount", totalAmount),
                    Map.entry("totalFees", totalFees),
                    Map.entry("netAmount", netAmount),
                    Map.entry("paymentsByMethod", methodStats),
                    Map.entry("paymentsByGateway", gatewayStats),
                    Map.entry("paymentsByStatus", statusStats),
                    Map.entry("dateRange", Map.ofEntries(
                            Map.entry("startDate", startDate.toString()),
                            Map.entry("endDate", endDate.toString())
                    ))
            );
        } catch (Exception e) {
            log.error("Error fetching payment statistics: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Private helper methods


    private void validatePaymentRequest(PaymentRequest request) {
        // Validate order and amount against Order Service
        try {
            com.blubugtech.bakery_payment_service.client.OrderClient.OrderDto order = orderClient.getOrderById(request.getOrderId());

            if (order == null) {
                throw new PaymentServiceException("Order not found: " + request.getOrderId());
            }

            if ("COMPLETED".equals(order.getPaymentStatus()) || "PAID".equals(order.getPaymentStatus())) {
                throw new PaymentServiceException("Order is already paid");
            }

            if ("CANCELLED".equals(order.getStatus())) {
                throw new PaymentServiceException("Cannot pay for a cancelled order");
            }

            if (request.getAmount().compareTo(order.getTotalAmount()) != 0) {
                throw new InvalidPaymentAmountException("Payment amount must match the order total: " + order.getTotalAmount());
            }
        } catch (feign.FeignException.NotFound e) {
            throw new PaymentServiceException("Order not found: " + request.getOrderId());
        } catch (Exception e) {
            if (e instanceof PaymentServiceException || e instanceof InvalidPaymentAmountException) {
                throw e;
            }
            log.error("Failed to validate order {}: {}", request.getOrderId(), e.getMessage(), e);
            throw new PaymentServiceException("Failed to validate order details");
        }

        if (request.getAmount().compareTo(minPaymentAmount) < 0) {
            throw new InvalidPaymentAmountException("Payment amount is below minimum: " + minPaymentAmount);
        }

        if (request.getAmount().compareTo(maxPaymentAmount) > 0) {
            throw new InvalidPaymentAmountException("Payment amount exceeds maximum: " + maxPaymentAmount);
        }

        // Check daily limit for user
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        BigDecimal dailyTotal = paymentRepository.getTotalAmountByDateRange(startOfDay, endOfDay);
        if (dailyTotal.add(request.getAmount()).compareTo(dailyPaymentLimit) > 0) {
            throw new PaymentServiceException("Daily payment limit exceeded");
        }
    }

    private void validateStatusTransition(@NonNull PaymentStatus currentStatus, @NonNull PaymentStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == PaymentStatus.PROCESSING ||
                    newStatus == PaymentStatus.CANCELLED ||
                    newStatus == PaymentStatus.COMPLETED;
            case PROCESSING -> newStatus == PaymentStatus.COMPLETED ||
                    newStatus == PaymentStatus.FAILED ||
                    newStatus == PaymentStatus.CANCELLED;
            case COMPLETED -> newStatus == PaymentStatus.REFUNDED;
            case FAILED -> newStatus == PaymentStatus.PROCESSING; // For retries
            case CANCELLED, REFUNDED -> false; // Terminal states
        };

        if (!isValidTransition) {
            throw new InvalidPaymentStatusException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void handleStatusTransition(Payment payment, PaymentStatus oldStatus,
                                        @NonNull PaymentStatus newStatus, String reason) {
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case COMPLETED -> {
                payment.setCapturedAt(now);
                if (payment.getAuthorizedAt() == null) {
                    payment.setAuthorizedAt(now);
                }
            }
            case FAILED -> {
                payment.setFailedAt(now);
                payment.setFailureReason(reason);
            }
            case CANCELLED -> {
                payment.setCancelledAt(now);
                payment.setFailureReason(reason);
            }
            case REFUNDED -> {
                payment.setFailureReason(reason);
            }
        }
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to convert metadata to JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }


    public void sendOtp(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        otpService.generateAndSendOtp(paymentId.toString(), payment.getUserId().toString(), null);
    }

    public PaymentResponse verifyOtp(UUID paymentId, String otp) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        boolean isValid = otpService.verifyOtp(paymentId.toString(), otp);
        if (!isValid) {
            throw new PaymentServiceException("Invalid or expired OTP");
        }

        // OTP valid, process payment
        paymentProcessingService.processPaymentAsync(payment);
        return paymentMapper.toResponse(payment);
    }
}
