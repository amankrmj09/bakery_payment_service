package com.blubugtech.bakery_payment_service.validation;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentRequest;
import com.blubugtech.bakery_payment_service.exception.payment.InvalidPaymentAmountException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentValidator {

    public void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException("Payment amount must be greater than zero");
        }
    }
}
