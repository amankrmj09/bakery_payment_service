package com.blubugtech.bakery_payment_service.integration.payment;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentGatewayResult {
    private final boolean success;
    private final boolean pending;
    private final String gatewayTransactionId;
    private final String gatewayResponse;
    private final String rawResponse;
    private final String failureCode;
    @Builder.Default
    private final BigDecimal gatewayFee = BigDecimal.ZERO;
}
