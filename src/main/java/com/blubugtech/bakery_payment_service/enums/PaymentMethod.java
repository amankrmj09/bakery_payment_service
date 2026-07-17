package com.blubugtech.bakery_payment_service.enums;

public enum PaymentMethod {
    CASH(PaymentGatewayProvider.MANUAL),
    CARD(PaymentGatewayProvider.STRIPE),
    DIGITAL_WALLET(PaymentGatewayProvider.PAYPAL),
    BANK_TRANSFER(PaymentGatewayProvider.MANUAL),
    CRYPTO(PaymentGatewayProvider.MOCK);

    private final PaymentGatewayProvider defaultProvider;

    PaymentMethod(PaymentGatewayProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public PaymentGatewayProvider getDefaultProvider() {
        return defaultProvider;
    }
}
