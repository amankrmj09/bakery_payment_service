package com.shah_s.bakery_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.devofblue.common.dto.RevenuePayloadDto;

@FeignClient(name = "bakery-auth-service", contextId = "internalStatsClient", path = "/api/users/internal/stats")
public interface InternalStatsClient {

    @PostMapping(value = "/add-revenue", headers = "X-User-Role=SYSTEM")
    void addRevenue(@RequestBody RevenuePayloadDto payload);
}
