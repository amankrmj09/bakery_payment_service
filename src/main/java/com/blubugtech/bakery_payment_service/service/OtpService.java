package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.integration.kafka.producer.UserEventPublisher;
import com.blubugtech.common.event.UserEvent;
import com.blubugtech.common.contract.messaging.UserPayload;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final UserEventPublisher userEventPublisher;
    private final Random random = new Random();
    private static final String OTP_PREFIX = "payment_otp:";
    private static final long OTP_VALIDITY_MINUTES = 5;

    public OtpService(StringRedisTemplate redisTemplate, UserEventPublisher userEventPublisher) {
        this.redisTemplate = redisTemplate;
        this.userEventPublisher = userEventPublisher;
    }

    public String generateAndSendOtp(String paymentId, String userIdStr, String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(OTP_PREFIX + paymentId, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        
        System.out.println("Generated OTP for payment " + paymentId + ": " + otp);

        try {
            UUID userId = (userIdStr != null && !userIdStr.isEmpty()) ? UUID.fromString(userIdStr) : UUID.randomUUID();
            String targetEmail = (email != null && !email.isEmpty()) ? email : "user@example.com";

            UserPayload payload = UserPayload.builder()
                .userId(userId)
                .email(targetEmail)
                .action("OTP_REQUESTED")
                .otpCode(otp)
                .expiryMinutes((int) OTP_VALIDITY_MINUTES)
                .timestamp(LocalDateTime.now())
                .build();

            UserEvent event = UserEvent.builder()
                .eventType("USER_OTP_REQUESTED")
                .payload(payload)
                .build();
            
            userEventPublisher.publishUserEvent(event);
        } catch (Exception e) {
            System.err.println("Failed to publish OTP event: " + e.getMessage());
        }

        return otp;
    }

    public boolean verifyOtp(String paymentId, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + paymentId);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(OTP_PREFIX + paymentId);
            return true;
        }
        return false;
    }

    public String resendOtp(String paymentId, String userIdStr, String email) {
        String existingOtp = redisTemplate.opsForValue().get(OTP_PREFIX + paymentId);
        if (existingOtp == null) {
            return null;
        }
        return generateAndSendOtp(paymentId, userIdStr, email);
    }
}
