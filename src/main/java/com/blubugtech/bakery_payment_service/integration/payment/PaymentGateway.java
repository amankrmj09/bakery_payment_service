package com.blubugtech.bakery_payment_service.integration.payment;

import com.blubugtech.bakery_payment_service.enums.TransactionType;

import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.entity.Refund;
import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;

public interface PaymentGateway {
    PaymentGatewayProvider getProviderType();
    PaymentGatewayResult processPayment(Payment payment, TransactionType transactionType);
    PaymentGatewayResult processRefund(Refund refund);
}
