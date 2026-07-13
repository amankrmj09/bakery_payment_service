package com.shah_s.bakery_payment_service.dto;

import com.shah_s.bakery_payment_service.entity.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentStatusUpdateRequestDto {

    // Getters and Setters
    @NotNull(message = "Status is required")
    private Payment.PaymentStatus status;

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private String gatewayResponse;

    // Constructors
    public PaymentStatusUpdateRequestDto() {}

    public PaymentStatusUpdateRequestDto(Payment.PaymentStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

}
