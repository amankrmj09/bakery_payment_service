package com.blubugtech.bakery_payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;

@Setter
@Getter
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_transaction_payment", columnList = "payment_id"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_gateway", columnList = "gateway_transaction_id"),
    @Index(name = "idx_transaction_date", columnList = "created_at")
})
public class PaymentTransaction {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull(message = "Payment is required")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode = org.blubakery.common.core.constants.GlobalAppConstants.CURRENCY_CODE;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "gateway_raw_response", columnDefinition = "TEXT")
    private String gatewayRawResponse;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Constructors
    public PaymentTransaction() {}

    public PaymentTransaction(Payment payment, TransactionType transactionType, BigDecimal amount, String description) {
        this.payment = payment;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.currencyCode = payment.getCurrencyCode();
    }

    // Utility Methods
    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

}
