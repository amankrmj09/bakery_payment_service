package com.blubugtech.bakery_payment_service.event;

import com.blubugtech.bakery_payment_service.client.UserClient;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.integration.kafka.producer.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PaymentEventListener {

    private final UserClient userClient;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentEventListener(UserClient userClient, PaymentEventPublisher paymentEventPublisher) {
        this.userClient = userClient;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Async
    @EventListener
    public void handlePaymentStatusUpdated(PaymentStatusUpdatedApplicationEvent applicationEvent) {
        Payment payment = applicationEvent.getPayment();
        try {
            UserClient.UserDto userDto = userClient.getUserById(payment.getUserId());
            org.blubakery.common.messaging.event.PaymentEvent event = org.blubakery.common.messaging.event.PaymentEvent.builder().payload(
                    org.blubakery.common.messaging.contract.messaging.PaymentPayload.builder()
                            .paymentId(payment.getId())
                            .orderId(payment.getOrderId())
                            .userId(payment.getUserId())
                            .customerEmail(userDto != null ? userDto.getEmail() : null)
                            .customerPhone(userDto != null ? userDto.getPhone() : null)
                            .status(payment.getStatus().name())
                            .amount(payment.getAmount())
                            .refundAmount(payment.getStatus() == com.blubugtech.bakery_payment_service.enums.PaymentStatus.REFUNDED ? payment.getAmount() : null)
                            .refundReason(payment.getStatus() == com.blubugtech.bakery_payment_service.enums.PaymentStatus.REFUNDED ? (payment.getFailureReason() != null && !payment.getFailureReason().trim().isEmpty() ? payment.getFailureReason() : "Refund processed by Admin") : null)
                            .timestamp(LocalDateTime.now())
                            .build()
            ).build();
            paymentEventPublisher.publishPaymentStatusUpdated(event);
            log.debug("Payment status event published via listener for payment: {}", payment.getPaymentReference());
        } catch (Exception e) {
            log.error("Failed to publish payment event for {}: {}", payment.getPaymentReference(), e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleRefundProcessed(RefundProcessedApplicationEvent applicationEvent) {
        com.blubugtech.bakery_payment_service.entity.Refund refund = applicationEvent.getRefund();
        Payment payment = refund.getPayment();
        try {
            UserClient.UserDto userDto = userClient.getUserById(payment.getUserId());
            org.blubakery.common.messaging.event.PaymentEvent event = org.blubakery.common.messaging.event.PaymentEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType("PAYMENT_REFUNDED")
                    .timestamp(java.time.Instant.now())
                    .payload(
                            org.blubakery.common.messaging.contract.messaging.PaymentPayload.builder()
                                    .paymentId(payment.getId())
                                    .orderId(payment.getOrderId())
                                    .userId(payment.getUserId())
                                    .customerEmail(userDto != null ? userDto.getEmail() : null)
                                    .customerPhone(userDto != null ? userDto.getPhone() : null)
                                    .status("REFUNDED")
                                    .amount(payment.getAmount())
                                    .refundAmount(refund.getAmount())
                                    .refundReason(refund.getReason())
                                    .timestamp(LocalDateTime.now())
                                    .build()
                    ).build();
            paymentEventPublisher.publishPaymentStatusUpdated(event);
            log.info("Published PAYMENT_REFUNDED event via listener for refund: {}", refund.getRefundReference());
        } catch (Exception e) {
            log.error("Failed to publish refund event for refund {}: {}", refund.getRefundReference(), e.getMessage(), e);
        }
    }
}
