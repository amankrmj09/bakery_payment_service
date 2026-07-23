package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.enums.PaymentStatus;

import com.blubugtech.bakery_payment_service.dto.payment.*;
import com.blubugtech.bakery_payment_service.dto.refund.*;
import com.blubugtech.bakery_payment_service.dto.transaction.*;
import com.blubugtech.bakery_payment_service.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse getPaymentById(UUID paymentId);
    PaymentResponse getPaymentByReference(String paymentReference);
    PaymentResponse getPaymentByOrderId(UUID orderId);
    List<PaymentResponse> getPaymentsByUserId(UUID userId);
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);
    Page<PaymentResponse> getAllPayments(Pageable pageable);
    PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatusUpdateRequest request);
    PaymentResponse cancelPayment(UUID paymentId, String reason);
    PaymentResponse retryPayment(UUID paymentId);
    Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);
    void sendOtp(UUID paymentId);
    PaymentResponse verifyOtp(UUID paymentId, String otp);
}
