package com.blubugtech.bakery_payment_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/stripe")
@Tag(name = "Stripe Webhook", description = "Endpoints for Stripe webhooks")
@Slf4j
public class StripeWebhookController {

    @PostMapping
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody Map<String, Object> payload, @RequestHeader Map<String, String> headers) {
        log.info("Received Stripe webhook: {}", payload);
        // Process webhook logic
        return ResponseEntity.ok().build();
    }
}
