package com.blubugtech.bakery_payment_service.integration.kafka.producer;

import org.blubakery.bakery_common_libs.constants.KafkaTopics;
import org.blubakery.bakery_common_libs.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentStatusUpdated(PaymentEvent event) {
        logger.info("Publishing PaymentStatusUpdated event for payment ID: {}", event.getPayload().getPaymentId());
        kafkaTemplate.send(KafkaTopics.PAYMENTS_TOPIC, event.getPayload().getPaymentId().toString(), event);
    }
}
