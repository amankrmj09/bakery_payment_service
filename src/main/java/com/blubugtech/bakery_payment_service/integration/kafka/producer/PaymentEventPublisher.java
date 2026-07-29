package com.blubugtech.bakery_payment_service.integration.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.messaging.constants.KafkaTopics;
import org.blubakery.common.messaging.event.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentStatusUpdated(PaymentEvent event) {
        log.info("Publishing PaymentStatusUpdated event for payment ID: {}", event.getPayload().getPaymentId());
        kafkaTemplate.send(KafkaTopics.PAYMENTS_TOPIC, event.getPayload().getPaymentId().toString(), event);
    }
}
