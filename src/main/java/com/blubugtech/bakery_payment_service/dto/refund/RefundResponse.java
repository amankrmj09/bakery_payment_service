package com.blubugtech.bakery_payment_service.dto.refund;

import com.blubugtech.bakery_payment_service.entity.Refund;
import com.blubugtech.bakery_payment_service.enums.RefundStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private UUID id;
    private String refundReference;
    private UUID paymentId;
    private String paymentReference;
    
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;
    
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    
    private String currencyCode;
    private String reason;
    private String gatewayRefundId;
    private String gatewayResponse;
    private String failureReason;
    private String failureCode;
    private UUID requestedBy;
    private UUID approvedBy;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String notes;
    private Map<String, Object> metadata;
}
