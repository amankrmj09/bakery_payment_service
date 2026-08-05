package com.blubugtech.bakery_payment_service.entity;

import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;
import com.blubugtech.bakery_payment_service.enums.PaymentMethod;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_reference", columnList = "payment_reference"),
        @Index(name = "idx_payment_order", columnList = "order_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_method", columnList = "payment_method"),
        @Index(name = "idx_payment_gateway", columnList = "payment_gateway"),
        @Index(name = "idx_payment_date", columnList = "created_at"),
        @Index(name = "idx_external_transaction", columnList = "external_transaction_id")
})
public class Payment {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_reference", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    @Column(name = "order_id", nullable = false)
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway", nullable = false)
    private PaymentGatewayProvider paymentGateway = PaymentGatewayProvider.MOCK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = org.blubakery.common.core.constants.GlobalAppConstants.CURRENCY_CODE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Payment method specific details
    @Column(name = "card_last_four", length = 4)
    @Size(max = 4, message = "Card last four must be 4 digits")
    private String cardLastFour;

    @Column(name = "card_brand", length = 20)
    @Size(max = 20, message = "Card brand must not exceed 20 characters")
    private String cardBrand; // VISA, MASTERCARD, AMEX, etc.

    @Column(name = "card_type", length = 20)
    @Size(max = 20, message = "Card type must not exceed 20 characters")
    private String cardType; // CREDIT, DEBIT

    @Column(name = "digital_wallet_provider", length = 50)
    @Size(max = 50, message = "Wallet provider must not exceed 50 characters")
    private String digitalWalletProvider; // PayPal, Apple Pay, Google Pay, etc.

    @Column(name = "bank_name", length = 100)
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    // Gateway specific fields
    @Column(name = "gateway_payment_id", length = 100)
    @Size(max = 100, message = "Gateway payment ID must not exceed 100 characters")
    private String gatewayPaymentId;

    @Column(name = "external_transaction_id", length = 100)
    @Size(max = 100, message = "External transaction ID must not exceed 100 characters")
    private String externalTransactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "gateway_raw_response", columnDefinition = "TEXT")
    private String gatewayRawResponse;

    // Payment status tracking
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Fee and settlement information
    @Column(name = "gateway_fee", precision = 8, scale = 2)
    @DecimalMin(value = "0.00", message = "Gateway fee cannot be negative")
    private BigDecimal gatewayFee = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Net amount cannot be negative")
    private BigDecimal netAmount;

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;

    // Relationships
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<PaymentTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<Refund> refunds = new ArrayList<>();

    // Metadata
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public Payment() {
    }

    public Payment(UUID orderId, UUID userId, PaymentMethod paymentMethod, BigDecimal amount, String description) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.description = description;
        this.paymentReference = generatePaymentReference();
        this.netAmount = amount.setScale(2, java.math.RoundingMode.HALF_UP); // Initially same as amount, updated after fees
    }

    // Utility Methods
    public void addTransaction(PaymentTransaction transaction) {
        transactions.add(transaction);
        transaction.setPayment(this);
    }

    public void addRefund(Refund refund) {
        refunds.add(refund);
        refund.setPayment(this);
    }

    public BigDecimal getTotalRefundedAmount() {
        return refunds.stream()
                .filter(refund -> refund.getStatus() == com.blubugtech.bakery_payment_service.enums.RefundStatus.COMPLETED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getRefundableAmount() {
        if (status != PaymentStatus.COMPLETED) {
            return BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return amount.subtract(getTotalRefundedAmount()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED && getRefundableAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canBeRetried() {
        return status == PaymentStatus.FAILED && retryCount < 3;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    public void calculateNetAmount() {
        this.netAmount = amount.subtract(gatewayFee != null ? gatewayFee : BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String generatePaymentReference() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.valueOf((int) (Math.random() * 9000) + 1000);
        return "PAY-" + timestamp + "-" + randomPart;
    }

}
