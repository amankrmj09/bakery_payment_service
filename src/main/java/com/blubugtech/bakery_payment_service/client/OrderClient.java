package com.blubugtech.bakery_payment_service.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "bakery-order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    OrderDto getOrderById(@PathVariable UUID id);

    @Setter
    @Getter
    class OrderDto {
        private UUID id;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String status;

    }
}
