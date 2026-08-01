package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.entity.Payment;

public interface PaymentProcessingService {
    void processPaymentAsync(Payment payment);
}
