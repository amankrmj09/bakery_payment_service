package com.blubugtech.bakery_payment_service.service.impl;

import com.blubugtech.bakery_payment_service.client.UserClient;
import com.blubugtech.bakery_payment_service.dto.refund.RefundRequest;
import com.blubugtech.bakery_payment_service.dto.refund.RefundResponse;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.Refund;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.enums.RefundStatus;
import com.blubugtech.bakery_payment_service.exception.payment.PaymentServiceException;
import com.blubugtech.bakery_payment_service.exception.refund.InvalidRefundException;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGateway;
import com.blubugtech.bakery_payment_service.mapper.RefundMapper;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.repository.RefundRepository;
import com.blubugtech.bakery_payment_service.service.RefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    final private RefundRepository refundRepository;

    final private PaymentRepository paymentRepository;

    final private List<PaymentGateway> paymentGateways;

    final private ObjectMapper objectMapper;

    final private ApplicationEventPublisher applicationEventPublisher;
    final private UserClient userClient;
    final private RefundMapper refundMapper;


    // Create refund
    public RefundResponse createRefund(RefundRequest request) {
        log.info("Creating refund for payment: {} amount: {}", request.getPaymentId(), request.getAmount());

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

            log.info("Refund created successfully: {}", savedRefund.getRefundReference());
            return refundMapper.toResponse(savedRefund);

        } catch (Exception e) {
            log.error("Failed to create refund for payment {}: {}", request.getPaymentId(), e.getMessage(), e);
            throw new PaymentServiceException("Failed to create refund: " + e.getMessage());
        }
    }

    // Get refund by ID
    @Transactional(readOnly = true)
    public RefundResponse getRefundById(UUID refundId) {
        log.debug("Fetching refund by ID: {}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with ID: " + refundId));

        return refundMapper.toResponse(refund);
    }

    // Get refund by reference
    @Transactional(readOnly = true)
    public RefundResponse getRefundByReference(String refundReference) {
        log.debug("Fetching refund by reference: {}", refundReference);

        Refund refund = refundRepository.findByRefundReference(refundReference)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with reference: " + refundReference));

        return refundMapper.toResponse(refund);
    }

    // Get refunds by payment ID
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getRefundsByPaymentId(UUID paymentId, Pageable pageable) {
        log.debug("Fetching refunds for payment: {}", paymentId);

        Page<RefundResponse> page = refundRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId, pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get refunds by status
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getRefundsByStatus(RefundStatus status, Pageable pageable) {
        log.debug("Fetching refunds by status: {}", status);

        Page<RefundResponse> page = refundRepository.findByStatus(status, pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get all refunds with pagination
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getAllRefunds(Pageable pageable) {
        log.debug("Fetching all refunds with pagination");

        Page<RefundResponse> page = refundRepository.findAll(pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get refunds by user
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getRefundsByUser(UUID userId, Pageable pageable) {
        log.debug("Fetching refunds requested by user: {}", userId);

        Page<RefundResponse> page = refundRepository.findByRequestedByOrderByCreatedAtDesc(userId, pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Approve refund
    public RefundResponse approveRefund(UUID refundId, UUID approvedBy) {
        log.info("Approving refund: {} by user: {}", refundId, approvedBy);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new PaymentServiceException("Only pending refunds can be approved");
        }

        refund.setStatus(RefundStatus.PROCESSING);
        refund.setApprovedBy(approvedBy);
        refund.setProcessedAt(LocalDateTime.now());

        Refund approvedRefund = refundRepository.save(refund);

        // Process refund asynchronously
        processRefundAsync(approvedRefund);

        log.info("Refund approved: {}", refundId);
        return refundMapper.toResponse(approvedRefund);
    }

    // Reject refund
    public RefundResponse rejectRefund(UUID refundId, String reason, UUID rejectedBy) {
        log.info("Rejecting refund: {} by user: {} reason: {}", refundId, rejectedBy, reason);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentServiceException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new PaymentServiceException("Only pending refunds can be rejected");
        }

        refund.setStatus(RefundStatus.FAILED);
        refund.setFailedAt(LocalDateTime.now());
        refund.setFailureReason(reason);
        refund.setApprovedBy(rejectedBy); // Track who rejected it

        Refund rejectedRefund = refundRepository.save(refund);
        log.info("Refund rejected: {}", refundId);

        return refundMapper.toResponse(rejectedRefund);
    }

    // Get pending refunds
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getPendingRefunds(Pageable pageable) {
        log.debug("Fetching pending refunds");

        Page<RefundResponse> page = refundRepository.findPendingRefunds(pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get completed refunds
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getCompletedRefunds(Pageable pageable) {
        log.debug("Fetching completed refunds");

        Page<RefundResponse> page = refundRepository.findCompletedRefunds(pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get failed refunds
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> getFailedRefunds(Pageable pageable) {
        log.debug("Fetching failed refunds");

        Page<RefundResponse> page = refundRepository.findFailedRefunds(pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get refund statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getRefundStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching refund statistics");

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
            log.error("Error fetching refund statistics: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Refund statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Search refunds
    @Transactional(readOnly = true)
    public org.springframework.data.web.PagedModel<RefundResponse> searchRefunds(String searchTerm, Pageable pageable) {
        log.debug("Searching refunds with term: {}", searchTerm);

        Page<RefundResponse> page = refundRepository.searchRefundsByText(searchTerm, pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Get refunds with filters
    @Transactional(readOnly = true)
    @Override
    public org.springframework.data.web.PagedModel<RefundResponse> getRefundsWithFilters(RefundStatus status, UUID requestedBy,
                                                      UUID approvedBy, BigDecimal minAmount, BigDecimal maxAmount,
                                                      LocalDateTime startDate, LocalDateTime endDate, org.springframework.data.domain.Pageable pageable) {
        log.debug("Fetching refunds with filters");

        Page<RefundResponse> page = refundRepository.findRefundsWithFilters(status, requestedBy, approvedBy,
                        minAmount, maxAmount, startDate, endDate, pageable)
                .map(refundMapper::toResponse);
        return new org.springframework.data.web.PagedModel<>(page);
    }

    // Private helper methods
    @Async
    protected void processRefundAsync(Refund refund) {
        log.info("Processing refund asynchronously: {}", refund.getRefundReference());

        try {
            // Process through gateway
            com.blubugtech.bakery_payment_service.integration.payment.PaymentGatewayResult gatewayResponse = getGatewayForMethod(refund.getPayment().getPaymentMethod()).processRefund(refund);

            // Update refund based on gateway response
            if (gatewayResponse.isSuccess()) {
                refund.setStatus(RefundStatus.COMPLETED);
                refund.setCompletedAt(LocalDateTime.now());
                publishRefundEvent(refund);
            } else if (gatewayResponse.isPending()) {
                refund.setStatus(RefundStatus.PROCESSING);
            } else {
                refund.setStatus(RefundStatus.FAILED);
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

            log.info("Refund processing completed: {} status: {}",
                    refund.getRefundReference(), refund.getStatus());

        } catch (Exception e) {
            log.error("Refund processing failed: {} - {}", refund.getRefundReference(), e.getMessage(), e);

            // Update refund as failed
            refund.setStatus(RefundStatus.FAILED);
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
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            log.info("Payment {} marked as fully refunded", payment.getPaymentReference());
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

    private void publishRefundEvent(Refund refund) {
        try {
            applicationEventPublisher.publishEvent(new com.blubugtech.bakery_payment_service.event.RefundProcessedApplicationEvent(this, refund));
            log.info("Published PAYMENT_REFUNDED application event for refund: {}", refund.getRefundReference());
        } catch (Exception e) {
            log.error("Failed to publish refund event for refund {}: {}", refund.getRefundReference(), e.getMessage(), e);
        }
    }

    private PaymentGateway getGatewayForMethod(com.blubugtech.bakery_payment_service.enums.PaymentMethod method) {
        com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider provider = method.getDefaultProvider();
        return paymentGateways.stream()
                .filter(g -> g.getProviderType() == provider)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No gateway available for provider: " + provider));
    }


}
