package com.blubugtech.bakery_payment_service.validation;

import com.blubugtech.bakery_payment_service.dto.refund.RefundRequest;
import com.blubugtech.bakery_payment_service.exception.refund.InvalidRefundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RefundValidator {

    public void validateRefundRequest(RefundRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundException("Refund amount must be greater than zero");
        }
    }
}
