package com.blubugtech.bakery_payment_service.integration.payment;

import com.blubugtech.bakery_payment_service.enums.TransactionType;

import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.entity.Refund;
import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class StripeGateway implements PaymentGateway {

    @Override
    public PaymentGatewayProvider getProviderType() {
        return PaymentGatewayProvider.STRIPE;
    }

    @Override
    public PaymentGatewayResult processPayment(Payment payment, TransactionType transactionType) {
        boolean success = ThreadLocalRandom.current().nextDouble() < 0.90;

        if (success) {
            return PaymentGatewayResult.builder()
                    .success(true)
                    .pending(false)
                    .gatewayTransactionId(generateTransactionId())
                    .gatewayResponse("Transaction approved")
                    .rawResponse(generateSuccessResponse(payment, transactionType))
                    .gatewayFee(BigDecimal.ZERO)
                    .build();
        } else {
            return PaymentGatewayResult.builder()
                    .success(false)
                    .pending(false)
                    .gatewayTransactionId(generateTransactionId())
                    .gatewayResponse("Card declined (Stripe)")
                    .rawResponse(generateFailureResponse("Card declined (Stripe)"))
                    .failureCode("DECLINED")
                    .build();
        }
    }

    @Override
    public PaymentGatewayResult processRefund(Refund refund) {
        return PaymentGatewayResult.builder()
                .success(true)
                .pending(false)
                .gatewayTransactionId(generateTransactionId())
                .gatewayResponse("Refund processed successfully")
                .rawResponse(generateRefundSuccessResponse(refund))
                .build();
    }

    private String generateTransactionId() {
        return "GW-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(10000, 99999);
    }
    
    private String generateSuccessResponse(Payment payment, TransactionType transactionType) {
        return String.format(
            "{\"status\":\"success\",\"transaction_type\":\"%s\",\"amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
            transactionType.name().toLowerCase(),
            payment.getAmount().toString(),
            payment.getCurrencyCode(),
            LocalDateTime.now().toString()
        );
    }

    private String generateFailureResponse(String failureReason) {
        return String.format(
            "{\"status\":\"failed\",\"error\":\"%s\",\"timestamp\":\"%s\"}",
            failureReason,
            LocalDateTime.now().toString()
        );
    }

    private String generateRefundSuccessResponse(Refund refund) {
        return String.format(
            "{\"status\":\"success\",\"refund_amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
            refund.getAmount().toString(),
            refund.getCurrencyCode(),
            LocalDateTime.now().toString()
        );
    }
}
