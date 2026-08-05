package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.otp.OtpErrorResponse;
import com.blubugtech.bakery_payment_service.dto.otp.OtpMessageResponse;
import com.blubugtech.bakery_payment_service.dto.otp.OtpSendResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OtpMapper {

    default OtpSendResponse toOtpSendResponse(String message, String mockOtp) {
        return OtpSendResponse.builder()
                .message(message)
                .mockOtp(mockOtp)
                .build();
    }

    default OtpMessageResponse toOtpMessageResponse(String message) {
        return OtpMessageResponse.builder()
                .message(message)
                .build();
    }

    default OtpErrorResponse toOtpErrorResponse(String error) {
        return OtpErrorResponse.builder()
                .error(error)
                .build();
    }
}
