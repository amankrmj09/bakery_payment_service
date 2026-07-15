package com.blubugtech.bakery_payment_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RefundRequestDto {

    // Getters and Setters
    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @NotNull(message = "Requested by user ID is required")
    private UUID requestedBy;

    private String notes;

    private Map<String, Object> metadata;

    // Constructors
    public RefundRequestDto() {}

    public RefundRequestDto(UUID paymentId, BigDecimal amount, String reason, UUID requestedBy) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.reason = reason;
        this.requestedBy = requestedBy;
    }

}
