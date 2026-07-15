package com.shah_s.bakery_payment_service.service;

import org.devofblue.common.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${kafka.topic.payment-events}")
    private String paymentEventsTopic;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishPaymentStatusUpdated(PaymentEvent event) {
        logger.info("Publishing PaymentStatusUpdated event for payment ID: {}", event.getPaymentId());
        kafkaTemplate.send(paymentEventsTopic, event.getPaymentId().toString(), event);
    }
}
