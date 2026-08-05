package com.blubugtech.bakery_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsResponse {
    private Long totalPayments;
    private Long successfulPayments;
    private Long failedPayments;
    private Long pendingPayments;
    private BigDecimal totalAmount;
    private BigDecimal totalFees;
    private BigDecimal netAmount;
    private List<PaymentMethodStatResponse> paymentsByMethod;
    private List<PaymentGatewayStatResponse> paymentsByGateway;
    private List<PaymentStatusStatResponse> paymentsByStatus;
    private DateRangeResponse dateRange;
}
