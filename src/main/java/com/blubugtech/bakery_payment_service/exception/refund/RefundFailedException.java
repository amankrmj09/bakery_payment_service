package com.blubugtech.bakery_payment_service.exception.refund;

public class RefundFailedException extends RuntimeException {
    public RefundFailedException(String message) {
        super(message);
    }
}
