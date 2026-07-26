package com.blubugtech.bakery_payment_service.integration.kafka.consumer;

import com.blubugtech.bakery_payment_service.dto.refund.RefundRequest;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.service.RefundService;
import com.blubugtech.common.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final PaymentRepository paymentRepository;
    private final RefundService refundService;

    public OrderEventListener(PaymentRepository paymentRepository, RefundService refundService) {
        this.paymentRepository = paymentRepository;
        this.refundService = refundService;
    }

    @KafkaListener(topics = "${kafka.topic.order-events}", groupId = "payment-service-group")
    public void consumeOrderEvent(OrderEvent event) {
        logger.info("Received OrderEvent with type: {} for Order ID: {}", event.getEventType(), event.getPayload().getOrderId());
        
        if ("ORDER_STATUS_UPDATED".equals(event.getEventType()) && "CANCELLED".equals(event.getPayload().getStatus())) {
            
            boolean cancelledByAdmin = event.getMetadata() != null && 
                    Boolean.TRUE.equals(event.getMetadata().get("cancelledByAdmin"));
            
            if (!cancelledByAdmin) {
                logger.info("Order {} was cancelled, but not by an admin. Skipping automatic refund.", event.getPayload().getOrderId());
                return;
            }

            logger.info("Order {} was cancelled by an admin, checking for completed payments to refund...", event.getPayload().getOrderId());
            
            // Query for completed payments associated with this order
            java.util.Optional<Payment> optionalPayment = paymentRepository.findByOrderId(event.getPayload().getOrderId());
            if (optionalPayment.isPresent()) {
                Payment payment = optionalPayment.get();
                if (payment.getStatus() == PaymentStatus.COMPLETED && payment.canBeRefunded()) {
                    logger.info("Found completed payment {} for cancelled order {}, initiating automatic refund.", payment.getId(), event.getPayload().getOrderId());
                    
                    try {
                        RefundRequest refundRequest = new RefundRequest();
                        refundRequest.setPaymentId(payment.getId());
                        refundRequest.setAmount(payment.getRefundableAmount());
                        refundRequest.setReason("Automatic refund due to order cancellation");
                        refundRequest.setRequestedBy(event.getPayload().getUserId()); // System or user who cancelled
                        
                        refundService.createRefund(refundRequest);
                        logger.info("Successfully initiated refund for payment {}", payment.getId());
                    } catch (Exception e) {
                        logger.error("Failed to process automatic refund for cancelled order {}: {}", event.getPayload().getOrderId(), e.getMessage());
                    }
                }
            }
        }
    }
}
