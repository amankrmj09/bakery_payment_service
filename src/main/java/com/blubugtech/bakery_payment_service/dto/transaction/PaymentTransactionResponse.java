package com.blubugtech.bakery_payment_service.dto.transaction;

import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
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
public class PaymentTransactionResponse {

    private UUID id;
    private TransactionType transactionType;
    
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    
    private String currencyCode;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String failureReason;
    private String failureCode;
    private String description;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime processedAt;
    private Map<String, Object> metadata;
}
