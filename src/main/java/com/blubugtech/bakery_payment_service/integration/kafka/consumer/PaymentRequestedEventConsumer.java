package com.blubugtech.bakery_payment_service.integration.kafka.consumer;

import com.blubugtech.bakery_payment_service.dto.payment.PaymentRequest;
import com.blubugtech.bakery_payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.messaging.constants.KafkaTopics;
import org.blubakery.common.messaging.event.PaymentRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestedEventConsumer {
    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_REQUESTS_TOPIC, groupId = "payment-service-group")
    public void consume(PaymentRequestedEvent event) {
        log.info("Received PaymentRequestedEvent for Order ID: {} with amount: {}", event.getPayload().getOrderId(), event.getPayload().getAmount());

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
            log.info("Successfully initiated payment for order: {}", event.getPayload().getOrderId());
        } catch (Exception e) {
            log.error("Failed to process payment request event for order {}: {}", event.getPayload().getOrderId(), e.getMessage(), e);
            // In a real system, you might want to publish a failed PaymentEvent here if the error is unrecoverable
        }
    }
}
