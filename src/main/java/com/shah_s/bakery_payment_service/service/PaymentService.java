package com.shah_s.bakery_payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shah_s.bakery_payment_service.client.OrderServiceClient;
import com.shah_s.bakery_payment_service.client.NotificationServiceClient;
import com.shah_s.bakery_payment_service.dto.*;
import com.shah_s.bakery_payment_service.entity.Payment;
import com.shah_s.bakery_payment_service.entity.PaymentTransaction;
import com.shah_s.bakery_payment_service.exception.PaymentServiceException;
import com.shah_s.bakery_payment_service.repository.PaymentRepository;
import com.shah_s.bakery_payment_service.service.PaymentGatewayService.PaymentGatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import com.shah_s.bakery_payment_service.exception.*;
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
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    final private PaymentRepository paymentRepository;

    final private PaymentTransactionService paymentTransactionService;

    final private RefundService refundService;

    final private PaymentGatewayService paymentGatewayService;

    final private OrderServiceClient orderServiceClient;
    
    final private NotificationServiceClient notificationServiceClient;

    final private ObjectMapper objectMapper;

    @Value("${payment.limits.min-amount:0.50}")
    private BigDecimal minPaymentAmount;

    @Value("${payment.limits.max-amount:10000.00}")
    private BigDecimal maxPaymentAmount;

    @Value("${payment.limits.daily-limit:50000.00}")
    private BigDecimal dailyPaymentLimit;

    public PaymentService(PaymentRepository paymentRepository, PaymentTransactionService paymentTransactionService, RefundService refundService, PaymentGatewayService paymentGatewayService, OrderServiceClient orderServiceClient, NotificationServiceClient notificationServiceClient, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionService = paymentTransactionService;
        this.refundService = refundService;
        this.paymentGatewayService = paymentGatewayService;
        this.orderServiceClient = orderServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.objectMapper = objectMapper;
    }

    // Create payment
    public PaymentResponse createPayment(PaymentRequest request) {
        logger.info("Creating payment for order: {} amount: {}", request.getOrderId(), request.getAmount());

        try {
            // Validate payment request
            validatePaymentRequest(request);

            // Check if payment already exists for order
            Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
            if (existingPayment.isPresent()) {
                throw new PaymentServiceException("Payment already exists for order: " + request.getOrderId());
            }

            // Verify order exists
            Map<String, Object> orderInfo = orderServiceClient.getOrderById(request.getOrderId());
            if (orderInfo == null) {
                throw new OrderNotFoundException("Order not found: " + request.getOrderId());
            }

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
            if (request.getPaymentMethod() != Payment.PaymentMethod.CASH) {
                payment.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            }

            // Save payment
            Payment savedPayment = paymentRepository.save(payment);

            // Process payment asynchronously
            processPaymentAsync(savedPayment);

            logger.info("Payment created successfully: {}", savedPayment.getPaymentReference());
            return PaymentResponse.from(savedPayment);

        } catch (Exception e) {
            logger.error("Failed to create payment for order {}: {}", request.getOrderId(), e.getMessage());
            throw new PaymentServiceException("Failed to create payment: " + e.getMessage());
        }
    }

    // Get payment by ID
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        logger.debug("Fetching payment by ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + paymentId));

        return PaymentResponse.from(payment);
    }

    // Get payment by reference
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String paymentReference) {
        logger.debug("Fetching payment by reference: {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with reference: " + paymentReference));

        return PaymentResponse.from(payment);
    }

    // Get payment by order ID
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        logger.debug("Fetching payment by order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found for order: " + orderId));

        return PaymentResponse.from(payment);
    }

    // Get payments by user ID
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(UUID userId) {
        logger.debug("Fetching payments for user: {}", userId);

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // Get payments by status
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        logger.debug("Fetching payments by status: {}", status);

        return paymentRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // Get all payments with pagination
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        logger.debug("Fetching all payments with pagination");

        return paymentRepository.findAll(pageable)
                .map(PaymentResponse::from);
    }

    // Update payment status
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatusUpdateRequest request) {
        logger.info("Updating payment status: {} to {}", paymentId, request.getStatus());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + paymentId));

        // Validate status transition
        validateStatusTransition(payment.getStatus(), request.getStatus());

        Payment.PaymentStatus oldStatus = payment.getStatus();
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

        logger.info("Payment status updated successfully: {} from {} to {}",
                   paymentId, oldStatus, request.getStatus());

        return PaymentResponse.from(updatedPayment);
    }

    // Cancel payment
    public PaymentResponse cancelPayment(UUID paymentId, String reason) {
        logger.info("Cancelling payment: {} with reason: {}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            throw new PaymentServiceException("Cannot cancel completed payment. Use refund instead.");
        }

        if (payment.getStatus() == Payment.PaymentStatus.CANCELLED) {
            throw new PaymentServiceException("Payment is already cancelled");
        }

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        payment.setFailureReason(reason);

        // Try to void the payment at gateway if it was authorized
        if (payment.getAuthorizedAt() != null) {
            try {
                PaymentGatewayResponse voidResponse = paymentGatewayService.voidPayment(payment);
                payment.setGatewayResponse(voidResponse.getGatewayResponse());
                payment.setGatewayRawResponse(voidResponse.getRawResponse());
            } catch (Exception e) {
                logger.warn("Failed to void payment at gateway: {}", e.getMessage());
            }
        }

        Payment cancelledPayment = paymentRepository.save(payment);

        // Notify order service
        notifyOrderServiceAsync(cancelledPayment);

        logger.info("Payment cancelled successfully: {}", paymentId);
        return PaymentResponse.from(cancelledPayment);
    }

    // Retry failed payment
    public PaymentResponse retryPayment(UUID paymentId) {
        logger.info("Retrying failed payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + paymentId));

        if (!payment.canBeRetried()) {
            throw new PaymentServiceException("Payment cannot be retried");
        }

        payment.incrementRetryCount();
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setFailureReason(null);
        payment.setFailureCode(null);

        Payment savedPayment = paymentRepository.save(payment);

        // Process payment asynchronously
        processPaymentAsync(savedPayment);

        logger.info("Payment retry initiated: {}", paymentId);
        return PaymentResponse.from(savedPayment);
    }

    // Get payment statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching payment statistics");

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
            logger.error("Error fetching payment statistics: {}", e.getMessage());
            return Map.of(
                    "error", "Statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Private helper methods
    @Async
    protected void processPaymentAsync(Payment payment) {
        logger.info("Processing payment asynchronously: {}", payment.getPaymentReference());

        try {
            // Update status to processing
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            // Process through gateway
            PaymentGatewayResponse gatewayResponse = paymentGatewayService.processPayment(payment);

            // Create transaction record
            PaymentTransaction transaction = new PaymentTransaction(payment,
                    PaymentTransaction.TransactionType.SALE, payment.getAmount(),
                    "Payment processing");

            // Update payment based on gateway response
            if (gatewayResponse.isSuccess()) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setCapturedAt(LocalDateTime.now());
                payment.setGatewayFee(gatewayResponse.getGatewayFee());
                payment.calculateNetAmount();
                transaction.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
                transaction.setProcessedAt(LocalDateTime.now());
            } else if (gatewayResponse.isPending()) {
                payment.setStatus(Payment.PaymentStatus.PENDING);
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailedAt(LocalDateTime.now());
                payment.setFailureReason(gatewayResponse.getGatewayResponse());
                payment.setFailureCode(gatewayResponse.getFailureCode());
                transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
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

            logger.info("Payment processing completed: {} status: {}",
                       payment.getPaymentReference(), payment.getStatus());

        } catch (Exception e) {
            logger.error("Payment processing failed: {} - {}", payment.getPaymentReference(), e.getMessage());

            // Update payment as failed
            payment.setStatus(Payment.PaymentStatus.FAILED);
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
            Map<String, Object> paymentUpdate = Map.of(
                    "paymentId", payment.getId(),
                    "paymentReference", payment.getPaymentReference(),
                    "status", payment.getStatus().name(),
                    "amount", payment.getAmount(),
                    "gatewayResponse", payment.getGatewayResponse() != null ? payment.getGatewayResponse() : ""
            );

            orderServiceClient.updateOrderPaymentStatus(payment.getOrderId(), paymentUpdate);

            logger.debug("Order service notified of payment update: {}", payment.getPaymentReference());
        } catch (Exception e) {
            logger.error("Failed to notify order service for payment {}: {}",
                        payment.getPaymentReference(), e.getMessage());
        }
    }

    @Async
    protected void sendPaymentNotificationAsync(Payment payment) {
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED && payment.getStatus() != Payment.PaymentStatus.FAILED) {
            return;
        }
        
        try {
            Map<String, Object> orderInfo = orderServiceClient.getOrderById(payment.getOrderId());
            String email = orderInfo != null ? (String) orderInfo.get("customerEmail") : null;
            String name = orderInfo != null ? (String) orderInfo.get("customerName") : null;
            
            Map<String, Object> notificationReq = new java.util.HashMap<>();
            notificationReq.put("type", "EMAIL");
            if (email != null) notificationReq.put("recipientEmail", email);
            if (name != null) notificationReq.put("recipientName", name);
            notificationReq.put("source", "PAYMENT_SERVICE");
            notificationReq.put("userId", payment.getUserId());
            
            if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
                notificationReq.put("title", "Payment Receipt: " + payment.getPaymentReference());
                notificationReq.put("subject", "Payment Receipt: " + payment.getPaymentReference());
                notificationReq.put("content", "Your payment of $" + payment.getAmount() + " was successful.");
            } else {
                notificationReq.put("title", "Payment Failed: " + payment.getPaymentReference());
                notificationReq.put("subject", "Payment Failed: " + payment.getPaymentReference());
                notificationReq.put("content", "Your payment of $" + payment.getAmount() + " failed. Reason: " + payment.getFailureReason());
            }
            
            notificationServiceClient.sendNotification(notificationReq);
        } catch (Exception ex) {
            logger.error("Failed to send payment notification: {}", ex.getMessage());
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        // Retrieve order info
        Map<String, Object> orderInfo = orderServiceClient.getOrderById(request.getOrderId());
        if (orderInfo == null) {
            throw new OrderNotFoundException("Order not found: " + request.getOrderId());
        }
        // Extract totalAmount from orderInfo
        Object totalAmountObj = orderInfo.get("totalAmount");
        if (totalAmountObj == null) {
            throw new PaymentServiceException("Order total amount not found for order: " + request.getOrderId());
        }
        BigDecimal orderTotalAmount;
        try {
            orderTotalAmount = new BigDecimal(totalAmountObj.toString());
        } catch (Exception e) {
            throw new PaymentServiceException("Invalid order total amount for order: " + request.getOrderId());
        }
        // Compare payment amount with order total amount
        if (request.getAmount().compareTo(orderTotalAmount) != 0) {
            throw new PaymentServiceException("Payment amount (" + request.getAmount() + ") does not match order total amount (" + orderTotalAmount + ")");
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

    private void validateStatusTransition(Payment.PaymentStatus currentStatus, Payment.PaymentStatus newStatus) {
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == Payment.PaymentStatus.PROCESSING ||
                          newStatus == Payment.PaymentStatus.CANCELLED ||
                          newStatus == Payment.PaymentStatus.COMPLETED;
            case PROCESSING -> newStatus == Payment.PaymentStatus.COMPLETED ||
                             newStatus == Payment.PaymentStatus.FAILED ||
                             newStatus == Payment.PaymentStatus.CANCELLED;
            case COMPLETED -> newStatus == Payment.PaymentStatus.REFUNDED;
            case FAILED -> newStatus == Payment.PaymentStatus.PROCESSING; // For retries
            case CANCELLED, REFUNDED -> false; // Terminal states
        };

        if (!isValidTransition) {
            throw new InvalidPaymentStatusException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void handleStatusTransition(Payment payment, Payment.PaymentStatus oldStatus,
                                      Payment.PaymentStatus newStatus, String reason) {
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
            logger.warn("Failed to convert metadata to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
