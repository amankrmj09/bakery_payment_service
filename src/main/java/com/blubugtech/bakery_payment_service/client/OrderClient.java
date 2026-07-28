package com.blubugtech.bakery_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "bakery-order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    OrderDto getOrderById(@PathVariable("id") UUID id);
    
    class OrderDto {
        private UUID id;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String status;
        
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
