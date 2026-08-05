package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.transaction.PaymentTransactionResponse;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTransactionMapper {
    @Mapping(target = "metadata", ignore = true)
    PaymentTransactionResponse toResponse(PaymentTransaction transaction);

    @AfterMapping
    default void mapMetadata(PaymentTransaction transaction, @MappingTarget PaymentTransactionResponse response) {
        if (transaction.getMetadata() != null) {
            try {
                response.setMetadata(Map.of("raw", transaction.getMetadata()));
            } catch (Exception e) {
                response.setMetadata(Map.of("error", "Failed to parse metadata"));
            }
        }
    }
}
