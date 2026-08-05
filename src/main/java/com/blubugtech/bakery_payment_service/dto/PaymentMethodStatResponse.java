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
public class PaymentMethodStatResponse {
    private String paymentMethod;
    private Long paymentCount;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private BigDecimal totalFees;
}
