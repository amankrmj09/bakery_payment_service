package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentResponse;
import com.blubugtech.bakery_payment_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PaymentTransactionMapper.class, RefundMapper.class})
public interface PaymentMapper {
    @Mapping(target = "refundableAmount", expression = "java(payment.getRefundableAmount())")
    @Mapping(target = "totalRefundedAmount", expression = "java(payment.getTotalRefundedAmount())")
    @Mapping(target = "canBeRefunded", expression = "java(payment.canBeRefunded())")
    @Mapping(target = "metadata", ignore = true)
    PaymentResponse toResponse(Payment payment);
}
