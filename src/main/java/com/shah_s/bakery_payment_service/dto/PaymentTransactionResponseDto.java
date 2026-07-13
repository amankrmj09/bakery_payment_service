package com.shah_s.bakery_payment_service.dto;

import com.shah_s.bakery_payment_service.entity.PaymentTransaction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class PaymentTransactionResponseDto {

    // Getters and Setters
    private UUID id;
    private PaymentTransaction.TransactionType transactionType;
    private PaymentTransaction.TransactionStatus status;
    private BigDecimal amount;
    private String currencyCode;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String failureReason;
    private String failureCode;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Map<String, Object> metadata;

    // Constructors
    public PaymentTransactionResponseDto() {}

    // Static factory method
    public static PaymentTransactionResponseDto from(PaymentTransaction transaction) {
        PaymentTransactionResponseDto response = new PaymentTransactionResponseDto();
        response.id = transaction.getId();
        response.transactionType = transaction.getTransactionType();
        response.status = transaction.getStatus();
        response.amount = transaction.getAmount();
        response.currencyCode = transaction.getCurrencyCode();
        response.gatewayTransactionId = transaction.getGatewayTransactionId();
        response.gatewayResponse = transaction.getGatewayResponse();
        response.failureReason = transaction.getFailureReason();
        response.failureCode = transaction.getFailureCode();
        response.description = transaction.getDescription();
        response.createdAt = transaction.getCreatedAt();
        response.processedAt = transaction.getProcessedAt();

        // Parse metadata JSON if exists
        if (transaction.getMetadata() != null) {
            try {
                response.metadata = Map.of("raw", transaction.getMetadata());
            } catch (Exception e) {
                response.metadata = Map.of("error", "Failed to parse metadata");
            }
        }

        return response;
    }

}
