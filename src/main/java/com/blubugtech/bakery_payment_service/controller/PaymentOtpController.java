package com.blubugtech.bakery_payment_service.controller;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentStatusUpdateRequest;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.service.OtpService;
import com.blubugtech.bakery_payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/mock")
@Tag(name = "Payment OTP", description = "Mock OTP Payment APIs")
@Slf4j
public class PaymentOtpController {

    private final OtpService otpService;
    private final PaymentService paymentService;

    public PaymentOtpController(OtpService otpService, PaymentService paymentService) {
        this.otpService = otpService;
        this.paymentService = paymentService;
    }

    @PostMapping("/{paymentId}/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email) {

        log.info("Sending OTP for payment ID: {}", paymentId);
        String otp = otpService.generateAndSendOtp(paymentId.toString(), userId, email);
        // Do not return OTP in production, only for learning purposes
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "mock_otp", otp));
    }

    @PostMapping("/{paymentId}/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@PathVariable UUID paymentId, @RequestBody Map<String, String> request) {
        String otp = request.get("otp");
        log.info("Verifying OTP for payment ID: {}", paymentId);

        boolean isValid = otpService.verifyOtp(paymentId.toString(), otp);
        if (isValid) {
            PaymentStatusUpdateRequest statusUpdate = new PaymentStatusUpdateRequest();
            statusUpdate.setStatus(PaymentStatus.COMPLETED);
            statusUpdate.setReason("OTP Verified successfully");
            paymentService.updatePaymentStatus(paymentId, statusUpdate);
            return ResponseEntity.ok(Map.of("message", "Payment completed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid or expired OTP"));
        }
    }

    @PostMapping("/{paymentId}/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email) {

        log.info("Resending OTP for payment ID: {}", paymentId);
        String otp = otpService.resendOtp(paymentId.toString(), userId, email);
        if (otp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Payment session expired. Please restart checkout."));
        }
        return ResponseEntity.ok(Map.of("message", "OTP resent successfully", "mock_otp", otp));
    }
}
