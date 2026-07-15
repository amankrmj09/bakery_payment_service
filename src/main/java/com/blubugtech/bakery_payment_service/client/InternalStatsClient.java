package com.blubugtech.bakery_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.blubugtech.common.dto.RevenuePayloadDto;

@FeignClient(name = "bakery-auth-service", contextId = "internalStatsClient", path = "/api/users/internal/stats", fallbackFactory = InternalStatsClientFallbackFactory.class)
public interface InternalStatsClient {

    @PostMapping(value = "/add-revenue", headers = "X-User-Role=SYSTEM")
    void addRevenue(@RequestBody RevenuePayloadDto payload);
}
