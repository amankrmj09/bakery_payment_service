package com.blubugtech.bakery_payment_service.client.statistics;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.core.exception.common.ServiceUnavailableException;
import org.blubakery.common.feign.exception.common.FeignClientException;
import org.blubakery.common.messaging.revenue.RevenuePayload;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InternalStatsClientFallbackFactory implements FallbackFactory<InternalStatsClient> {

    @Override
    public InternalStatsClient create(Throwable cause) {
        return new InternalStatsClient() {
            @Override
            public void addRevenue(RevenuePayload payload) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for addRevenue: could not add revenue {}", payload.getAmount(), cause);
                throw new ServiceUnavailableException("Auth Service is currently unavailable. Please try again later.", cause);
            }
        };
    }
}
