package com.blubugtech.bakery_payment_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();
    private static final String OTP_PREFIX = "payment_otp:";
    private static final long OTP_VALIDITY_MINUTES = 5;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndSendOtp(String paymentId) {
        String otp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(OTP_PREFIX + paymentId, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        
        // In a real scenario, this would send an SMS/Email.
        // For learning, we'll just log it or return it.
        System.out.println("Generated OTP for payment " + paymentId + ": " + otp);
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
}
