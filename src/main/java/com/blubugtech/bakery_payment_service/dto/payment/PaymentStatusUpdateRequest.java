package com.blubugtech.bakery_payment_service.dto.payment;

import com.blubugtech.bakery_payment_service.enums.PaymentStatus;

import com.blubugtech.bakery_payment_service.entity.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentStatusUpdateRequest {

    // Getters and Setters
    @NotNull(message = "Status is required")
    private PaymentStatus status;

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private String gatewayResponse;

    // Constructors
    public PaymentStatusUpdateRequest() {}

    public PaymentStatusUpdateRequest(PaymentStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

}
