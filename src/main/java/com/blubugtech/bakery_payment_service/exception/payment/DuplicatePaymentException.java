package com.blubugtech.bakery_payment_service.exception.payment;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String message) {
        super(message);
    }
}
