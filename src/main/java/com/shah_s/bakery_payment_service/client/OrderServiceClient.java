package com.shah_s.bakery_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "bakery-order-service", path = "/api/orders")
public interface OrderServiceClient {

    @GetMapping("/{orderId}")
    Map<String, Object> getOrderById(@PathVariable UUID orderId);

    @PatchMapping("/{orderId}/status")
    Map<String, Object> updateOrderStatus(@PathVariable UUID orderId, @RequestBody Map<String, String> request);

    @PostMapping("/{orderId}/payment-update")
    Map<String, Object> updateOrderPaymentStatus(@PathVariable UUID orderId, @RequestBody Map<String, Object> request);
}
