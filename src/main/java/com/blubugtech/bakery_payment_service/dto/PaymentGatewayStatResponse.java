package com.blubugtech.bakery_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayStatResponse {
    private String paymentGateway;
    private Long paymentCount;
    private BigDecimal totalAmount;
    private Long successfulPayments;
    private Long failedPayments;
}
