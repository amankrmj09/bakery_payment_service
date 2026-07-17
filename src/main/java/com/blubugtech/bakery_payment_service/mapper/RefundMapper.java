package com.blubugtech.bakery_payment_service.mapper;

import com.blubugtech.bakery_payment_service.dto.refund.RefundResponse;
import com.blubugtech.bakery_payment_service.entity.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefundMapper {
    @org.mapstruct.Mapping(target = "metadata", ignore = true)
    RefundResponse toResponse(Refund refund);
}
