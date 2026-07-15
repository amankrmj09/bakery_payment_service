package com.blubugtech.bakery_payment_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class HealthController {


    final private DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Main service health check
    @GetMapping("/health")
    public ResponseEntity<com.blubugtech.common.dto.HealthResponseDto> health() {
        com.blubugtech.common.dto.HealthResponseDto response = new com.blubugtech.common.dto.HealthResponseDto("UP", "bakery-payment-service");
        Map<String, Object> details = new HashMap<>();
        details.put("version", "1.0.0");

        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            details.put("database", "UP");
            details.put("databaseUrl", connection.getMetaData().getURL());
        } catch (Exception e) {
            details.put("database", "DOWN");
            details.put("databaseError", e.getMessage());
        }

        response.setDetails(details);
        return ResponseEntity.ok(response);
    }

    // Service info
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", "Bakery Payment Service");
        response.put("description", "Payment processing and gateway integration service");
        response.put("version", "1.0.0");
        response.put("features", Map.of(
            "payments", "Multi-gateway payment processing",
            "refunds", "Full and partial refund management",
            "transactions", "Complete transaction history tracking",
            "gateways", "Support for Stripe, PayPal, Square",
            "analytics", "Payment and refund analytics"
        ));
        response.put("endpoints", Map.of(
            "payments", "/api/payments",
            "refunds", "/api/refunds",
            "transactions", "/api/transactions"
        ));

        return ResponseEntity.ok(response);
    }

    // Service metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("uptime", getUptime());
        response.put("timestamp", LocalDateTime.now().toString());

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        memory.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        response.put("memory", memory);

        return ResponseEntity.ok(response);
    }

    private String getUptime() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d days, %d hours, %d minutes, %d seconds",
                days, hours % 24, minutes % 60, seconds % 60);
    }
}
