package com.blubugtech.bakery_payment_service.enums;

public enum PaymentMethod {
    CASH(PaymentGatewayProvider.MANUAL),
    CARD(PaymentGatewayProvider.MOCK),
    DIGITAL_WALLET(PaymentGatewayProvider.MOCK),
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
