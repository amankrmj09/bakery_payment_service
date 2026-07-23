package com.blubugtech.bakery_payment_service.integration.kafka.producer;

import com.blubugtech.common.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(UserEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${kafka.topic.user-events:user-events}")
    private String userEventsTopic;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishUserEvent(UserEvent event) {
        logger.info("Publishing UserEvent with action: {} for user ID: {}", 
                event.getPayload().getAction(), event.getPayload().getUserId());
        kafkaTemplate.send(userEventsTopic, event.getPayload().getUserId().toString(), event);
    }
}
