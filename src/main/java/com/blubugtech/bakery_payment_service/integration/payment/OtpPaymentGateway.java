package com.blubugtech.bakery_payment_service.integration.payment;

import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.Refund;
import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class OtpPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayProvider getProviderType() {
        return PaymentGatewayProvider.MOCK;
    }

    @Override
    public PaymentGatewayResult processPayment(Payment payment, TransactionType transactionType) {
        // Return pending because OTP verification is required
        return PaymentGatewayResult.builder()
                .success(false)
                .pending(true)
                .gatewayTransactionId(generateTransactionId())
                .gatewayResponse("Awaiting OTP verification")
                .rawResponse(generatePendingResponse(payment, transactionType))
                .gatewayFee(BigDecimal.ZERO)
                .build();
    }

    @Override
    public PaymentGatewayResult processRefund(Refund refund) {
        return PaymentGatewayResult.builder()
                .success(true)
                .pending(false)
                .gatewayTransactionId(generateTransactionId())
                .gatewayResponse("Refund processed successfully (Mock)")
                .rawResponse(generateRefundSuccessResponse(refund))
                .build();
    }

    private String generateTransactionId() {
        return "OTP-GW-" + System.currentTimeMillis();
    }

    private String generatePendingResponse(Payment payment, TransactionType transactionType) {
        return String.format(
                "{\"status\":\"pending_otp\",\"transaction_type\":\"%s\",\"amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
                transactionType.name().toLowerCase(),
                payment.getAmount().toString(),
                payment.getCurrencyCode(),
                LocalDateTime.now()
        );
    }

    private String generateRefundSuccessResponse(Refund refund) {
        return String.format(
                "{\"status\":\"success\",\"refund_amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
                refund.getAmount().toString(),
                refund.getCurrencyCode(),
                LocalDateTime.now()
        );
    }
}
