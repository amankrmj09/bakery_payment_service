package com.blubugtech.bakery_payment_service.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    
    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 8, message = "OTP must be between 4 and 8 characters")
    private String otp;
}
