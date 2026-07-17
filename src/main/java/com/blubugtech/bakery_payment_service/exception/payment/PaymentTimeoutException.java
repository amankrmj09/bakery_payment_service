package com.blubugtech.bakery_payment_service.exception.payment;

public class PaymentTimeoutException extends RuntimeException {
    public PaymentTimeoutException(String message) {
        super(message);
    }
}
