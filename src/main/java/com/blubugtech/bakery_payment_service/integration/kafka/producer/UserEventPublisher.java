package com.blubugtech.bakery_payment_service.integration.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.bakery_common_libs.constants.KafkaTopics;
import org.blubakery.bakery_common_libs.event.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserEvent(UserEvent event) {
        log.info("Publishing UserEvent with action: {} for user ID: {}",
                event.getPayload().getAction(), event.getPayload().getUserId());
        kafkaTemplate.send(KafkaTopics.USER_TOPIC, event.getPayload().getUserId().toString(), event);
    }
}
