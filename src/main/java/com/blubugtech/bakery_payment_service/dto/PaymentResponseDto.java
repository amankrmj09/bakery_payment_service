package com.blubugtech.bakery_payment_service.dto;

import com.blubugtech.bakery_payment_service.entity.Payment;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class PaymentResponseDto {

    // Getters and Setters (abbreviated for space)
    private UUID id;
    private String paymentReference;
    private UUID orderId;
    private UUID userId;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentGateway paymentGateway;
    private Payment.PaymentStatus status;
    private BigDecimal amount;
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
    private List<PaymentTransactionResponseDto> transactions;
    private List<RefundResponseDto> refunds;
    private BigDecimal totalRefundedAmount;
    private BigDecimal refundableAmount;
    private Boolean canBeRefunded;
    private Boolean isExpired;
    private Boolean canBeRetried;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime failedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;
    private String notes;
    private Map<String, Object> metadata;

    // Constructors
    public PaymentResponseDto() {}

    // Static factory method
    public static PaymentResponseDto from(Payment payment) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.id = payment.getId();
        response.paymentReference = payment.getPaymentReference();
        response.orderId = payment.getOrderId();
        response.userId = payment.getUserId();
        response.paymentMethod = payment.getPaymentMethod();
        response.paymentGateway = payment.getPaymentGateway();
        response.status = payment.getStatus();
        response.amount = payment.getAmount();
        response.currencyCode = payment.getCurrencyCode();
        response.description = payment.getDescription();
        response.cardLastFour = payment.getCardLastFour();
        response.cardBrand = payment.getCardBrand();
        response.cardType = payment.getCardType();
        response.digitalWalletProvider = payment.getDigitalWalletProvider();
        response.bankName = payment.getBankName();
        response.gatewayPaymentId = payment.getGatewayPaymentId();
        response.externalTransactionId = payment.getExternalTransactionId();
        response.gatewayResponse = payment.getGatewayResponse();
        response.failureReason = payment.getFailureReason();
        response.failureCode = payment.getFailureCode();
        response.retryCount = payment.getRetryCount();
        response.lastRetryAt = payment.getLastRetryAt();
        response.gatewayFee = payment.getGatewayFee();
        response.netAmount = payment.getNetAmount();
        response.settlementDate = payment.getSettlementDate();
        response.transactions = payment.getTransactions().stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
        response.refunds = payment.getRefunds().stream()
                .map(RefundResponseDto::from)
                .collect(Collectors.toList());
        response.totalRefundedAmount = payment.getTotalRefundedAmount();
        response.refundableAmount = payment.getRefundableAmount();
        response.canBeRefunded = payment.canBeRefunded();
        response.isExpired = payment.isExpired();
        response.canBeRetried = payment.canBeRetried();
        response.createdAt = payment.getCreatedAt();
        response.updatedAt = payment.getUpdatedAt();
        response.authorizedAt = payment.getAuthorizedAt();
        response.capturedAt = payment.getCapturedAt();
        response.failedAt = payment.getFailedAt();
        response.cancelledAt = payment.getCancelledAt();
        response.expiresAt = payment.getExpiresAt();
        response.notes = payment.getNotes();

        // Parse metadata JSON if exists
        if (payment.getMetadata() != null) {
            try {
                // Simple JSON parsing - in real app use ObjectMapper
                response.metadata = Map.of("raw", payment.getMetadata());
            } catch (Exception e) {
                response.metadata = Map.of("error", "Failed to parse metadata");
            }
        }

        return response;
    }

}
