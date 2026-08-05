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

import com.blubugtech.bakery_payment_service.dto.otp.OtpErrorResponse;
import com.blubugtech.bakery_payment_service.dto.otp.OtpMessageResponse;
import com.blubugtech.bakery_payment_service.dto.otp.OtpSendResponse;
import com.blubugtech.bakery_payment_service.dto.otp.VerifyOtpRequest;
import com.blubugtech.bakery_payment_service.mapper.OtpMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments/mock")
@Tag(name = "Payment OTP", description = "Mock OTP Payment APIs")
@Slf4j
public class PaymentOtpController {

    private final OtpService otpService;
    private final PaymentService paymentService;
    private final OtpMapper otpMapper;

    public PaymentOtpController(OtpService otpService, PaymentService paymentService, OtpMapper otpMapper) {
        this.otpService = otpService;
        this.paymentService = paymentService;
        this.otpMapper = otpMapper;
    }

    @PostMapping("/{paymentId}/send-otp")
    public ResponseEntity<OtpSendResponse> sendOtp(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email) {

        log.info("Sending OTP for payment ID: {}", paymentId);
        String otp = otpService.generateAndSendOtp(paymentId.toString(), userId, email);
        // Do not return OTP in production, only for learning purposes
        return ResponseEntity.ok(otpMapper.toOtpSendResponse("OTP sent successfully", otp));
    }

    @PostMapping("/{paymentId}/verify-otp")
    public ResponseEntity<?> verifyOtp(@PathVariable UUID paymentId, @RequestBody VerifyOtpRequest request) {
        String otp = request.getOtp();
        log.info("Verifying OTP for payment ID: {}", paymentId);

        boolean isValid = otpService.verifyOtp(paymentId.toString(), otp);
        if (isValid) {
            PaymentStatusUpdateRequest statusUpdate = new PaymentStatusUpdateRequest();
            statusUpdate.setStatus(PaymentStatus.COMPLETED);
            statusUpdate.setReason("OTP Verified successfully");
            paymentService.updatePaymentStatus(paymentId, statusUpdate);
            return ResponseEntity.ok(otpMapper.toOtpMessageResponse("Payment completed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(otpMapper.toOtpErrorResponse("Invalid or expired OTP"));
        }
    }

    @PostMapping("/{paymentId}/resend-otp")
    public ResponseEntity<?> resendOtp(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email) {

        log.info("Resending OTP for payment ID: {}", paymentId);
        String otp = otpService.resendOtp(paymentId.toString(), userId, email);
        if (otp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(otpMapper.toOtpErrorResponse("Payment session expired. Please restart checkout."));
        }
        return ResponseEntity.ok(otpMapper.toOtpSendResponse("OTP resent successfully", otp));
    }
}
