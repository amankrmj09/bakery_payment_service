package com.blubugtech.bakery_payment_service.client;

import com.blubugtech.common.dto.RevenuePayloadDto;
import com.blubugtech.common.exception.FeignClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class InternalStatsClientFallbackFactory implements FallbackFactory<InternalStatsClient> {

    private static final Logger logger = LoggerFactory.getLogger(InternalStatsClientFallbackFactory.class);

    @Override
    public InternalStatsClient create(Throwable cause) {
        return new InternalStatsClient() {
            @Override
            public void addRevenue(RevenuePayloadDto payload) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for addRevenue: could not add revenue {}", payload.getAmount(), cause);
            }
        };
    }
}
