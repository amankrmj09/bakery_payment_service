package com.shah_s.bakery_payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shah_s.bakery_payment_service.dto.RefundRequest;
import com.shah_s.bakery_payment_service.dto.RefundResponse;
import com.shah_s.bakery_payment_service.entity.Payment;
import com.shah_s.bakery_payment_service.entity.Refund;
import com.shah_s.bakery_payment_service.exception.PaymentServiceException;
import com.shah_s.bakery_payment_service.repository.PaymentRepository;
import com.shah_s.bakery_payment_service.repository.RefundRepository;
import com.shah_s.bakery_payment_service.service.PaymentGatewayService.PaymentGatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RefundService {

    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);

    final private RefundRepository refundRepository;

    final private PaymentRepository paymentRepository;

    final private PaymentGatewayService paymentGatewayService;

    final private ObjectMapper objectMapper;

    public RefundService(RefundRepository refundRepository, PaymentRepository paymentRepository, PaymentGatewayService paymentGatewayService, ObjectMapper objectMapper) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.objectMapper = objectMapper;
    }

    // Create refund
    public RefundResponse createRefund(RefundRequest request) {
        logger.info("Creating refund for payment: {} amount: {}", request.getPaymentId(), request.getAmount());

        try {
            // Get payment
            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + request.getPaymentId()));

            // Validate refund request
            validateRefundRequest(payment, request);

            // Create refund entity
            Refund refund = new Refund(payment, request.getAmount(), request.getReason(), request.getRequestedBy());
            refund.setNotes(request.getNotes());

            // Set metadata
            if (request.getMetadata() != null) {
                refund.setMetadata(convertMetadataToJson(request.getMetadata()));
            }

            // Save refund
            Refund savedRefund = refundRepository.save(refund);

            // Process refund asynchronously
            processRefundAsync(savedRefund);

            logger.info("Refund created successfully: {}", savedRefund.getRefundReference());
            return RefundResponse.from(savedRefund);

        } catch (Exception e) {
            logger.error("Failed to create refund for payment {}: {}", request.getPaymentId(), e.getMessage());
            throw new PaymentServiceException("Failed to create refund: " + e.getMessage());
        }
    }

    // Get refund by ID
    @Transactional(readOnly = true)
    public RefundResponse getRefundById(UUID refundId) {
        logger.debug("Fetching refund by ID: {}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with ID: " + refundId));

        return RefundResponse.from(refund);
    }

    // Get refund by reference
    @Transactional(readOnly = true)
    public RefundResponse getRefundByReference(String refundReference) {
        logger.debug("Fetching refund by reference: {}", refundReference);

        Refund refund = refundRepository.findByRefundReference(refundReference)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with reference: " + refundReference));

        return RefundResponse.from(refund);
    }

    // Get refunds by payment ID
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByPaymentId(UUID paymentId) {
        logger.debug("Fetching refunds for payment: {}", paymentId);

        return refundRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Get refunds by status
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByStatus(Refund.RefundStatus status) {
        logger.debug("Fetching refunds by status: {}", status);

        return refundRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Get all refunds with pagination
    @Transactional(readOnly = true)
    public Page<RefundResponse> getAllRefunds(Pageable pageable) {
        logger.debug("Fetching all refunds with pagination");

        return refundRepository.findAll(pageable)
                .map(RefundResponse::from);
    }

    // Get refunds by user
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByUser(UUID userId) {
        logger.debug("Fetching refunds requested by user: {}", userId);

        return refundRepository.findByRequestedByOrderByCreatedAtDesc(userId).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Approve refund
    public RefundResponse approveRefund(UUID refundId, UUID approvedBy) {
        logger.info("Approving refund: {} by user: {}", refundId, approvedBy);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new PaymentServiceException("Only pending refunds can be approved");
        }

        refund.setStatus(Refund.RefundStatus.PROCESSING);
        refund.setApprovedBy(approvedBy);
        refund.setProcessedAt(LocalDateTime.now());

        Refund approvedRefund = refundRepository.save(refund);

        // Process refund asynchronously
        processRefundAsync(approvedRefund);

        logger.info("Refund approved: {}", refundId);
        return RefundResponse.from(approvedRefund);
    }

    // Reject refund
    public RefundResponse rejectRefund(UUID refundId, String reason, UUID rejectedBy) {
        logger.info("Rejecting refund: {} by user: {} reason: {}", refundId, rejectedBy, reason);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new PaymentServiceException("Only pending refunds can be rejected");
        }

        refund.setStatus(Refund.RefundStatus.FAILED);
        refund.setFailedAt(LocalDateTime.now());
        refund.setFailureReason(reason);
        refund.setApprovedBy(rejectedBy); // Track who rejected it

        Refund rejectedRefund = refundRepository.save(refund);
        logger.info("Refund rejected: {}", refundId);

        return RefundResponse.from(rejectedRefund);
    }

    // Get pending refunds
    @Transactional(readOnly = true)
    public List<RefundResponse> getPendingRefunds() {
        logger.debug("Fetching pending refunds");

        return refundRepository.findPendingRefunds().stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Get completed refunds
    @Transactional(readOnly = true)
    public List<RefundResponse> getCompletedRefunds() {
        logger.debug("Fetching completed refunds");

        return refundRepository.findCompletedRefunds().stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Get failed refunds
    @Transactional(readOnly = true)
    public List<RefundResponse> getFailedRefunds() {
        logger.debug("Fetching failed refunds");

        return refundRepository.findFailedRefunds().stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Get refund statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getRefundStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching refund statistics");

        try {
            Object[] successRate = refundRepository.getRefundSuccessRate(startDate, endDate);
            List<Object[]> statusStats = refundRepository.getRefundStatisticsByStatus(startDate, endDate);
            BigDecimal totalRefundAmount = refundRepository.getTotalRefundAmountByDateRange(startDate, endDate);

            return Map.of(
                    "totalRefunds", successRate[0],
                    "successfulRefunds", successRate[1],
                    "failedRefunds", successRate[2],
                    "pendingRefunds", successRate[3],
                    "totalRefundAmount", totalRefundAmount,
                    "refundsByStatus", statusStats,
                    "dateRange", Map.of(
                            "startDate", startDate.toString(),
                            "endDate", endDate.toString()
                    )
            );
        } catch (Exception e) {
            logger.error("Error fetching refund statistics: {}", e.getMessage());
            return Map.of(
                    "error", "Refund statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Search refunds
    @Transactional(readOnly = true)
    public List<RefundResponse> searchRefunds(String searchTerm) {
        logger.debug("Searching refunds with term: {}", searchTerm);

        return refundRepository.searchRefundsByText(searchTerm).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Get refunds with filters
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsWithFilters(Refund.RefundStatus status, UUID requestedBy,
                                                    UUID approvedBy, BigDecimal minAmount, BigDecimal maxAmount,
                                                    LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching refunds with filters");

        return refundRepository.findRefundsWithFilters(status, requestedBy, approvedBy,
                                                      minAmount, maxAmount, startDate, endDate).stream()
                .map(RefundResponse::from)
                .collect(Collectors.toList());
    }

    // Private helper methods
    @Async
    protected void processRefundAsync(Refund refund) {
        logger.info("Processing refund asynchronously: {}", refund.getRefundReference());

        try {
            // Process through gateway
            PaymentGatewayResponse gatewayResponse = paymentGatewayService.processRefund(refund);

            // Update refund based on gateway response
            if (gatewayResponse.isSuccess()) {
                refund.setStatus(Refund.RefundStatus.COMPLETED);
                refund.setCompletedAt(LocalDateTime.now());
            } else if (gatewayResponse.isPending()) {
                refund.setStatus(Refund.RefundStatus.PROCESSING);
            } else {
                refund.setStatus(Refund.RefundStatus.FAILED);
                refund.setFailedAt(LocalDateTime.now());
                refund.setFailureReason(gatewayResponse.getGatewayResponse());
                refund.setFailureCode(gatewayResponse.getFailureCode());
            }

            // Update gateway information
            refund.setGatewayRefundId(gatewayResponse.getGatewayTransactionId());
            refund.setGatewayResponse(gatewayResponse.getGatewayResponse());
            refund.setGatewayRawResponse(gatewayResponse.getRawResponse());

            // Save refund
            refundRepository.save(refund);

            // Update payment status if fully refunded
            updatePaymentRefundStatus(refund.getPayment());

            logger.info("Refund processing completed: {} status: {}",
                       refund.getRefundReference(), refund.getStatus());

        } catch (Exception e) {
            logger.error("Refund processing failed: {} - {}", refund.getRefundReference(), e.getMessage());

            // Update refund as failed
            refund.setStatus(Refund.RefundStatus.FAILED);
            refund.setFailedAt(LocalDateTime.now());
            refund.setFailureReason("Refund processing error: " + e.getMessage());
            refundRepository.save(refund);
        }
    }

    private void validateRefundRequest(Payment payment, RefundRequest request) {
        // Check if payment can be refunded
        if (!payment.canBeRefunded()) {
            throw new PaymentServiceException("Payment cannot be refunded");
        }

        // Check refund amount
        if (request.getAmount().compareTo(payment.getRefundableAmount()) > 0) {
            throw new InvalidRefundException("Refund amount exceeds refundable amount: " + payment.getRefundableAmount());
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundException("Refund amount must be greater than zero");
        }
    }

    private void updatePaymentRefundStatus(Payment payment) {
        BigDecimal totalRefunded = payment.getTotalRefundedAmount();

        if (totalRefunded.compareTo(payment.getAmount()) >= 0) {
            // Payment is fully refunded
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            logger.info("Payment {} marked as fully refunded", payment.getPaymentReference());
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
