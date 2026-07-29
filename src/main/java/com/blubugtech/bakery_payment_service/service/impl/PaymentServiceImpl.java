package com.blubugtech.bakery_payment_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import com.blubugtech.bakery_payment_service.enums.PaymentMethod;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;

import com.blubugtech.bakery_payment_service.integration.kafka.producer.PaymentEventPublisher;
import com.blubugtech.bakery_payment_service.service.PaymentService;

import com.blubugtech.bakery_payment_service.service.PaymentTransactionService;
import com.blubugtech.bakery_payment_service.service.RefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.blubugtech.bakery_payment_service.client.statistics.InternalStatsClient;
import com.blubugtech.bakery_payment_service.dto.payment.*;
import com.blubugtech.bakery_payment_service.dto.refund.*;
import com.blubugtech.bakery_payment_service.dto.transaction.*;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.exception.payment.*;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGatewayResult;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGateway;
import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import com.blubugtech.bakery_payment_service.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    final private PaymentRepository paymentRepository;

    final private PaymentTransactionService paymentTransactionService;

    final private RefundService refundService;

    final private List<PaymentGateway> paymentGateways;


    final private InternalStatsClient internalStatsClient;

    final private ObjectMapper objectMapper;
    final private com.blubugtech.bakery_payment_service.client.UserClient userClient;

    final private PaymentEventPublisher paymentEventPublisher;

    final private com.blubugtech.bakery_payment_service.service.OtpService otpService;
    
    final private com.blubugtech.bakery_payment_service.client.OrderClient orderClient;

    final private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.limits.min-amount:0.50}")
    private BigDecimal minPaymentAmount;

    @Value("${payment.limits.max-amount:10000.00}")
    private BigDecimal maxPaymentAmount;

    @Value("${payment.limits.daily-limit:50000.00}")
    private BigDecimal dailyPaymentLimit;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentTransactionService paymentTransactionService, RefundService refundService, List<PaymentGateway> paymentGateways, ObjectMapper objectMapper, InternalStatsClient internalStatsClient, PaymentEventPublisher paymentEventPublisher, com.blubugtech.bakery_payment_service.service.OtpService otpService, org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate, com.blubugtech.bakery_payment_service.client.UserClient userClient, com.blubugtech.bakery_payment_service.client.OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionService = paymentTransactionService;
        this.refundService = refundService;
        
        this.objectMapper = objectMapper;
        this.internalStatsClient = internalStatsClient;
        this.paymentEventPublisher = paymentEventPublisher;
        this.paymentGateways = paymentGateways;
        this.otpService = otpService;
        this.kafkaTemplate = kafkaTemplate;
        this.userClient = userClient;
        this.orderClient = orderClient;
    }

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
            return PaymentResponse.from(savedPayment);

        } catch (Exception e) {
            log.error("Failed to create payment for order {}: {}", request.getOrderId(), e.getMessage());
            throw new PaymentServiceException("Failed to create payment: " + e.getMessage());
        }
    }

    // Get payment by ID
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        log.debug("Fetching payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        return PaymentResponse.from(payment);
    }

    // Get payment by reference
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String paymentReference) {
        log.debug("Fetching payment by reference: {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with reference: " + paymentReference));

        return PaymentResponse.from(payment);
    }

    // Get payment by order ID
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        log.debug("Fetching payment by order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));

        return PaymentResponse.from(payment);
    }

    // Get payments by user ID
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(UUID userId) {
        log.debug("Fetching payments for user: {}", userId);

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // Get payments by status
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        log.debug("Fetching payments by status: {}", status);

        return paymentRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // Get all payments with pagination
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        log.debug("Fetching all payments with pagination");

        return paymentRepository.findAll(pageable)
                .map(PaymentResponse::from);
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
        notifyOrderServiceAsync(updatedPayment);

        // Notify user
        sendPaymentNotificationAsync(updatedPayment);

        log.info("Payment status updated successfully: {} from {} to {}",
                   paymentId, oldStatus, request.getStatus());

        return PaymentResponse.from(updatedPayment);
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
                log.warn("Failed to void payment at gateway: {}", e.getMessage());
            }
        }

        Payment cancelledPayment = paymentRepository.save(payment);

        // Notify order service
        notifyOrderServiceAsync(cancelledPayment);

        log.info("Payment cancelled successfully: {}", paymentId);
        return PaymentResponse.from(cancelledPayment);
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
        processPaymentAsync(savedPayment);

        log.info("Payment retry initiated: {}", paymentId);
        return PaymentResponse.from(savedPayment);
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
            log.error("Error fetching payment statistics: {}", e.getMessage());
            return Map.of(
                    "error", "Statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Private helper methods
    @Async
    protected void processPaymentAsync(Payment payment) {
        log.info("Processing payment asynchronously: {}", payment.getPaymentReference());

        try {
            // Update status to processing
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            // Process through gateway
            PaymentGatewayResult gatewayResponse = getGatewayForMethod(payment.getPaymentMethod()).processPayment(payment, TransactionType.SALE);

            // Create transaction record
            PaymentTransaction transaction = new PaymentTransaction(payment,
                    TransactionType.SALE, payment.getAmount(),
                    "Payment processing");

            // Update payment based on gateway response
            if (gatewayResponse.isSuccess()) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCapturedAt(LocalDateTime.now());
                payment.setGatewayFee(gatewayResponse.getGatewayFee());
                payment.calculateNetAmount();
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setProcessedAt(LocalDateTime.now());
            } else if (gatewayResponse.isPending()) {
                payment.setStatus(PaymentStatus.PENDING);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(LocalDateTime.now());
                payment.setFailureReason(gatewayResponse.getGatewayResponse());
                payment.setFailureCode(gatewayResponse.getFailureCode());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason(gatewayResponse.getGatewayResponse());
                transaction.setFailureCode(gatewayResponse.getFailureCode());
            }

            // Update gateway information
            payment.setGatewayPaymentId(gatewayResponse.getGatewayTransactionId());
            payment.setGatewayResponse(gatewayResponse.getGatewayResponse());
            payment.setGatewayRawResponse(gatewayResponse.getRawResponse());

            transaction.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
            transaction.setGatewayResponse(gatewayResponse.getGatewayResponse());
            transaction.setGatewayRawResponse(gatewayResponse.getRawResponse());

            // Save payment and transaction
            payment.addTransaction(transaction);
            paymentRepository.save(payment);

            // Notify order service
            notifyOrderServiceAsync(payment);

            // Notify user
            sendPaymentNotificationAsync(payment);

            log.info("Payment processing completed: {} status: {}",
                       payment.getPaymentReference(), payment.getStatus());

        } catch (Exception e) {
            log.error("Payment processing failed: {} - {}", payment.getPaymentReference(), e.getMessage());

            // Update payment as failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailedAt(LocalDateTime.now());
            payment.setFailureReason("Payment processing error: " + e.getMessage());
            paymentRepository.save(payment);

            // Notify order service
            notifyOrderServiceAsync(payment);

            // Notify user
            sendPaymentNotificationAsync(payment);
        }
    }

    @Async
    protected void notifyOrderServiceAsync(Payment payment) {
        try {
            com.blubugtech.bakery_payment_service.client.UserClient.UserDto userDto = userClient.getUserById(payment.getUserId());
            org.blubakery.bakery_common_libs.event.PaymentEvent event = org.blubakery.bakery_common_libs.event.PaymentEvent.builder().payload(
                org.blubakery.bakery_common_libs.contract.messaging.PaymentPayload.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .customerEmail(userDto != null ? userDto.getEmail() : null)
                    .customerPhone(userDto != null ? userDto.getPhone() : null)
                    .status(payment.getStatus().name())
                    .amount(payment.getAmount())
                    .timestamp(LocalDateTime.now())
                    .build()
            ).build();
            paymentEventPublisher.publishPaymentStatusUpdated(event);
            log.debug("Payment status event published for payment: {}", payment.getPaymentReference());
        } catch (Exception e) {
            log.error("Failed to publish payment event for {}: {}",
                        payment.getPaymentReference(), e.getMessage());
        }
    }

    @Async
    protected void sendPaymentNotificationAsync(Payment payment) {
        // Notification handled asynchronously via Kafka PaymentEvent by NotificationService
    }

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
            log.error("Failed to validate order {}: {}", request.getOrderId(), e.getMessage());
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

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
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
                                      PaymentStatus newStatus, String reason) {
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
        }
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to convert metadata to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private PaymentGateway getGatewayForMethod(com.blubugtech.bakery_payment_service.enums.PaymentMethod method) {
        com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider provider = method.getDefaultProvider();
        return paymentGateways.stream()
                .filter(g -> g.getProviderType() == provider)
                .findFirst()
                .orElseThrow(() -> new PaymentServiceException("No gateway available for provider: " + provider));
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
        processPaymentAsync(payment);
        return PaymentResponse.from(payment);
    }
}
