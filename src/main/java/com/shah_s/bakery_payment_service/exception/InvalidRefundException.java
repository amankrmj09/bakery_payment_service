package com.shah_s.bakery_payment_service.exception;

public class InvalidRefundException extends RuntimeException {
    public InvalidRefundException(String message) {
        super(message);
    }
}
