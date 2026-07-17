package com.blubugtech.bakery_payment_service.exception.gateway;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }
}
