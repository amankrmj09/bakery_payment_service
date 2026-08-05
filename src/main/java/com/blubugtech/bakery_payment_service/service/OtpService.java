package com.blubugtech.bakery_payment_service.service;

public interface OtpService {
    String generateAndSendOtp(String paymentId, String userIdStr, String email);

    boolean verifyOtp(String paymentId, String otp);

    String resendOtp(String paymentId, String userIdStr, String email);
}
