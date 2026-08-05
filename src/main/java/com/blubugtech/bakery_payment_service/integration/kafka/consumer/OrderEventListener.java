package com.blubugtech.bakery_payment_service.integration.kafka.consumer;

import com.blubugtech.bakery_payment_service.dto.refund.RefundRequest;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.messaging.constants.KafkaTopics;
import org.blubakery.common.messaging.order.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentRepository paymentRepository;
    private final RefundService refundService;

    @KafkaListener(topics = KafkaTopics.ORDERS_TOPIC, groupId = "payment-service-group")
    public void consumeOrderEvent(OrderEvent event) {

        log.info("Received OrderEvent. EventType: {}, OrderId: {}",
                event.getEventType(),
                event.getPayload().getOrderId());

        // Only process cancelled order events
        if (!"ORDER_STATUS_UPDATED".equals(event.getEventType())
                || !"CANCELLED".equals(event.getPayload().getStatus())) {
            log.debug("Ignoring event. EventType: {}, Status: {}",
                    event.getEventType(),
                    event.getPayload().getStatus());
            return;
        }

        boolean cancelledByAdmin = event.getMetadata() != null
                && Boolean.TRUE.equals(event.getMetadata().get("cancelledByAdmin"));

        if (!cancelledByAdmin) {
            log.info("Order {} was cancelled by the customer. Automatic refund will not be initiated.",
                    event.getPayload().getOrderId());
            return;
        }

        log.info("Order {} was cancelled by an administrator. Checking for eligible payment...",
                event.getPayload().getOrderId());

        Optional<Payment> optionalPayment =
                paymentRepository.findByOrderId(event.getPayload().getOrderId());

        if (optionalPayment.isEmpty()) {
            log.warn("No payment found for cancelled order {}.",
                    event.getPayload().getOrderId());
            return;
        }

        Payment payment = optionalPayment.get();

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            log.warn("Payment {} is not completed. Current status: {}",
                    payment.getId(),
                    payment.getStatus());
            return;
        }

        if (!payment.canBeRefunded()) {
            log.warn("Payment {} is not eligible for refund.",
                    payment.getId());
            return;
        }

        log.info("Eligible payment {} found for order {}. Initiating automatic refund.",
                payment.getId(),
                event.getPayload().getOrderId());

        try {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPaymentId(payment.getId());
            refundRequest.setAmount(payment.getRefundableAmount());
            refundRequest.setReason("Automatic refund due to order cancellation");
            refundRequest.setRequestedBy(event.getPayload().getUserId());

            refundService.createRefund(refundRequest);

            log.info("Automatic refund successfully initiated for payment {}.",
                    payment.getId());

        } catch (Exception e) {
            log.error("Failed to initiate automatic refund for payment {} (Order {}).",
                    payment.getId(),
                    event.getPayload().getOrderId(),
                    e);
        }
    }
}
