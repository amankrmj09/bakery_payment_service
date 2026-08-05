package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentResponse;
import com.blubugtech.bakery_payment_service.entity.Payment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PaymentTransactionMapper.class, RefundMapper.class})
public interface PaymentMapper {
    @Mapping(target = "refundableAmount", expression = "java(payment.getRefundableAmount())")
    @Mapping(target = "totalRefundedAmount", expression = "java(payment.getTotalRefundedAmount())")
    @Mapping(target = "canBeRefunded", expression = "java(payment.canBeRefunded())")
    @Mapping(target = "metadata", ignore = true)
    PaymentResponse toResponse(Payment payment);

    @AfterMapping
    default void mapMetadata(Payment payment, @MappingTarget PaymentResponse response) {
        if (payment.getMetadata() != null) {
            try {
                // Simple JSON parsing - in real app use ObjectMapper
                response.setMetadata(Map.of("raw", payment.getMetadata()));
            } catch (Exception e) {
                response.setMetadata(Map.of("error", "Failed to parse metadata"));
            }
        }
    }
}
