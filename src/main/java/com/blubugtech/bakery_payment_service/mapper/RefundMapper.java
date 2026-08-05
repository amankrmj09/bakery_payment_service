package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.refund.RefundResponse;
import com.blubugtech.bakery_payment_service.entity.Refund;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefundMapper {
    @org.mapstruct.Mapping(target = "paymentId", source = "payment.id")
    @org.mapstruct.Mapping(target = "paymentReference", source = "payment.paymentReference")
    @org.mapstruct.Mapping(target = "metadata", ignore = true)
    RefundResponse toResponse(Refund refund);

    @AfterMapping
    default void mapMetadata(Refund refund, @MappingTarget RefundResponse response) {
        if (refund.getMetadata() != null) {
            try {
                response.setMetadata(Map.of("raw", refund.getMetadata()));
            } catch (Exception e) {
                response.setMetadata(Map.of("error", "Failed to parse metadata"));
            }
        }
    }
}
