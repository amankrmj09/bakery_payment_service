package com.shah_s.bakery_payment_service.dto;

import com.shah_s.bakery_payment_service.entity.Refund;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class RefundResponseDto {

    // Getters and Setters
    private UUID id;
    private String refundReference;
    private UUID paymentId;
    private String paymentReference;
    private Refund.RefundStatus status;
    private BigDecimal amount;
    private String currencyCode;
    private String reason;
    private String gatewayRefundId;
    private String gatewayResponse;
    private String failureReason;
    private String failureCode;
    private UUID requestedBy;
    private UUID approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String notes;
    private Map<String, Object> metadata;

    // Constructors
    public RefundResponseDto() {}

    // Static factory method
    public static RefundResponseDto from(Refund refund) {
        RefundResponseDto response = new RefundResponseDto();
        response.id = refund.getId();
        response.refundReference = refund.getRefundReference();
        response.paymentId = refund.getPayment().getId();
        response.paymentReference = refund.getPayment().getPaymentReference();
        response.status = refund.getStatus();
        response.amount = refund.getAmount();
        response.currencyCode = refund.getCurrencyCode();
        response.reason = refund.getReason();
        response.gatewayRefundId = refund.getGatewayRefundId();
        response.gatewayResponse = refund.getGatewayResponse();
        response.failureReason = refund.getFailureReason();
        response.failureCode = refund.getFailureCode();
        response.requestedBy = refund.getRequestedBy();
        response.approvedBy = refund.getApprovedBy();
        response.createdAt = refund.getCreatedAt();
        response.updatedAt = refund.getUpdatedAt();
        response.processedAt = refund.getProcessedAt();
        response.completedAt = refund.getCompletedAt();
        response.failedAt = refund.getFailedAt();
        response.notes = refund.getNotes();

        // Parse metadata JSON if exists
        if (refund.getMetadata() != null) {
            try {
                response.metadata = Map.of("raw", refund.getMetadata());
            } catch (Exception e) {
                response.metadata = Map.of("error", "Failed to parse metadata");
            }
        }

        return response;
    }

}
