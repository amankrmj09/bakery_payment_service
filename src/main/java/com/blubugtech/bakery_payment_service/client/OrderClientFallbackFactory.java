package com.blubugtech.bakery_payment_service.client;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.core.exception.common.ServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {
    @Override
    public OrderClient create(Throwable cause) {
        return new OrderClient() {
            @Override
            public OrderDto getOrderById(UUID id) {
                log.error("Error calling order service for order id: {}", id, cause);
                throw new ServiceUnavailableException("Order service is currently unavailable. Please try again later.");
            }
        };
    }
}
