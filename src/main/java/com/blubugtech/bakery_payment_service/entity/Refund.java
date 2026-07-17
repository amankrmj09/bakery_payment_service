package com.blubugtech.bakery_payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.blubugtech.bakery_payment_service.enums.RefundStatus;

@Setter
@Getter
@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refund_payment", columnList = "payment_id"),
    @Index(name = "idx_refund_reference", columnList = "refund_reference"),
    @Index(name = "idx_refund_status", columnList = "status"),
    @Index(name = "idx_refund_date", columnList = "created_at"),
    @Index(name = "idx_refund_gateway", columnList = "gateway_refund_id")
})
public class Refund {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "refund_reference", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Refund reference is required")
    private String refundReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull(message = "Payment is required")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode = "USD";

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "gateway_refund_id", length = 100)
    private String gatewayRefundId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "gateway_raw_response", columnDefinition = "TEXT")
    private String gatewayRawResponse;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "requested_by", nullable = false)
    @NotNull(message = "Requested by user ID is required")
    private UUID requestedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Constructors
    public Refund() {}

    public Refund(Payment payment, BigDecimal amount, String reason, UUID requestedBy) {
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
        this.requestedBy = requestedBy;
        this.currencyCode = payment.getCurrencyCode();
        this.refundReference = generateRefundReference();
    }

    // Utility Methods
    public boolean isCompleted() {
        return status == RefundStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == RefundStatus.FAILED;
    }

    public boolean isPending() {
        return status == RefundStatus.PENDING;
    }

    private String generateRefundReference() {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.valueOf((int) (Math.random() * 900) + 100);
        return "REF-" + timestamp + "-" + randomPart;
    }

}
