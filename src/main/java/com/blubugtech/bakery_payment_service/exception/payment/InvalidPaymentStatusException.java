package com.blubugtech.bakery_payment_service.exception.payment;

import com.blubugtech.bakery_payment_service.enums.PaymentStatus;

public class InvalidPaymentStatusException extends RuntimeException {
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
