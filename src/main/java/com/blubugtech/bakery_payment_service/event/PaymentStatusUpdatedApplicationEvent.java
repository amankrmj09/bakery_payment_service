package com.blubugtech.bakery_payment_service.event;

import com.blubugtech.bakery_payment_service.entity.Payment;
import org.springframework.context.ApplicationEvent;

public class PaymentStatusUpdatedApplicationEvent extends ApplicationEvent {
    private final Payment payment;

    public PaymentStatusUpdatedApplicationEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}
