package com.blubugtech.bakery_payment_service.dto.payment;

import com.blubugtech.bakery_payment_service.dto.refund.RefundResponse;
import com.blubugtech.bakery_payment_service.dto.transaction.PaymentTransactionResponse;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;
import com.blubugtech.bakery_payment_service.enums.PaymentMethod;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private String paymentReference;
    private UUID orderId;
    private UUID userId;
    private PaymentMethod paymentMethod;
    private PaymentGatewayProvider paymentGateway;
    
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    
    private String currencyCode;
    private String description;
    private String cardLastFour;
    private String cardBrand;
    private String cardType;
    private String digitalWalletProvider;
    private String bankName;
    private String gatewayPaymentId;
    private String externalTransactionId;
    private String gatewayResponse;
    private String failureReason;
    private String failureCode;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private BigDecimal gatewayFee;
    private BigDecimal netAmount;
    private LocalDateTime settlementDate;
    
    @Builder.Default
    private List<PaymentTransactionResponse> transactions = new ArrayList<>();
    
    @Builder.Default
    private List<RefundResponse> refunds = new ArrayList<>();
    
    @Builder.Default
    private BigDecimal totalRefundedAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal refundableAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private Boolean canBeRefunded = false;
    
    private Boolean isExpired;
    private Boolean canBeRetried;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime failedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;
    private String notes;
    private Map<String, Object> metadata;
}
