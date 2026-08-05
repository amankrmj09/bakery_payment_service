package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentRequest;
import com.blubugtech.bakery_payment_service.dto.payment.PaymentResponse;
import com.blubugtech.bakery_payment_service.dto.payment.PaymentStatusUpdateRequest;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import org.springframework.data.web.PagedModel;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse getPaymentById(UUID paymentId);

    PaymentResponse getPaymentByReference(String paymentReference);

    PaymentResponse getPaymentByOrderId(UUID orderId);

    PagedModel<PaymentResponse> getPaymentsByUserId(UUID userId, Pageable pageable);

    PagedModel<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable);

    PagedModel<PaymentResponse> getAllPayments(Pageable pageable);

    PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatusUpdateRequest request);

    PaymentResponse cancelPayment(UUID paymentId, String reason);

    PaymentResponse retryPayment(UUID paymentId);

    Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);

    void sendOtp(UUID paymentId);

    PaymentResponse verifyOtp(UUID paymentId, String otp);
}
