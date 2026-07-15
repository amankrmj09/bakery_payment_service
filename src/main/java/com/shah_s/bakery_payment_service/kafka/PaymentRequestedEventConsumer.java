package com.shah_s.bakery_payment_service.kafka;

import org.devofblue.common.event.PaymentRequestedEvent;
import com.shah_s.bakery_payment_service.dto.PaymentRequestDto;
import com.shah_s.bakery_payment_service.service.PaymentService;
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
        logger.info("Received PaymentRequestedEvent for Order ID: {} with amount: {}", event.getOrderId(), event.getAmount());
        
        try {
            PaymentRequestDto request = new PaymentRequestDto();
            request.setOrderId(event.getOrderId());
            request.setUserId(event.getUserId());
            request.setPaymentMethod(com.shah_s.bakery_payment_service.entity.Payment.PaymentMethod.valueOf(event.getPaymentMethod()));
            request.setAmount(event.getAmount());
            request.setCurrencyCode(event.getCurrencyCode());
            request.setDescription("Payment for order");
            request.setCardLastFour(event.getCardLastFour());
            request.setCardBrand(event.getCardBrand());
            request.setCardType(event.getCardType());
            request.setDigitalWalletProvider(event.getDigitalWalletProvider());
            request.setBankName(event.getBankName());
            request.setNotes(event.getNotes());
            
            paymentService.createPayment(request);
            logger.info("Successfully initiated payment for order: {}", event.getOrderId());
        } catch (Exception e) {
            logger.error("Failed to process payment request event for order {}: {}", event.getOrderId(), e.getMessage());
            // In a real system, you might want to publish a failed PaymentEvent here if the error is unrecoverable
        }
    }
}
