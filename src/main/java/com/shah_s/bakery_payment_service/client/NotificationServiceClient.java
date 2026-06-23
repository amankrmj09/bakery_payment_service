package com.shah_s.bakery_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "bakery-notification-service", path = "/api/notifications")
public interface NotificationServiceClient {

    @PostMapping(headers = {"X-User-Role=SYSTEM", "X-User-Id=00000000-0000-0000-0000-000000000000"})
    Map<String, Object> sendNotification(@RequestBody Map<String, Object> request);
}
