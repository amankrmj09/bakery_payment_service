package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.transaction.PaymentTransactionResponse;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTransactionMapper {
    @Mapping(target = "metadata", ignore = true)
    PaymentTransactionResponse toResponse(PaymentTransaction transaction);
}
