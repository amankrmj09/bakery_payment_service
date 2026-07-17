package com.blubugtech.bakery_payment_service.integration.kafka.consumer;

import com.blubugtech.bakery_payment_service.enums.PaymentMethod;

import com.blubugtech.common.event.PaymentRequestedEvent;
import com.blubugtech.bakery_payment_service.dto.payment.*;
import com.blubugtech.bakery_payment_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentRequestedEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRequestedEventConsumer.class);
    private final PaymentService paymentService;

    public PaymentRequestedEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${kafka.topic.payment-requests}", groupId = "payment-service-group")
    public void consume(PaymentRequestedEvent event) {
        logger.info("Received PaymentRequestedEvent for Order ID: {} with amount: {}", event.getPayload().getOrderId(), event.getPayload().getAmount());
        
        try {
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(event.getPayload().getOrderId());
            request.setUserId(event.getPayload().getUserId());
            request.setPaymentMethod(com.blubugtech.bakery_payment_service.enums.PaymentMethod.valueOf(event.getPayload().getPaymentMethod()));
            request.setAmount(event.getPayload().getAmount());
            request.setCurrencyCode(event.getPayload().getCurrencyCode());
            request.setDescription("Payment for order");
            request.setCardLastFour(event.getPayload().getCardLastFour());
            request.setCardBrand(event.getPayload().getCardBrand());
            request.setCardType(event.getPayload().getCardType());
            request.setDigitalWalletProvider(event.getPayload().getDigitalWalletProvider());
            request.setBankName(event.getPayload().getBankName());
            request.setNotes(event.getPayload().getNotes());
            
            paymentService.createPayment(request);
            logger.info("Successfully initiated payment for order: {}", event.getPayload().getOrderId());
        } catch (Exception e) {
            logger.error("Failed to process payment request event for order {}: {}", event.getPayload().getOrderId(), e.getMessage());
            // In a real system, you might want to publish a failed PaymentEvent here if the error is unrecoverable
        }
    }
}
