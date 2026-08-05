package com.blubugtech.bakery_payment_service.dto.otp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendResponse {
    private String message;
    
    @JsonProperty("mock_otp")
    private String mockOtp;
}
