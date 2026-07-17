package com.blubugtech.bakery_payment_service.exception.refund;

public class InvalidRefundException extends RuntimeException {
    public InvalidRefundException(String message) {
        super(message);
    }
}
