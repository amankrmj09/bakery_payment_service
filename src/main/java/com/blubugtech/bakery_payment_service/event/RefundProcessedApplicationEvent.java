package com.blubugtech.bakery_payment_service.event;

import com.blubugtech.bakery_payment_service.entity.Refund;
import org.springframework.context.ApplicationEvent;

public class RefundProcessedApplicationEvent extends ApplicationEvent {
    private final Refund refund;

    public RefundProcessedApplicationEvent(Object source, Refund refund) {
        super(source);
        this.refund = refund;
    }

    public Refund getRefund() {
        return refund;
    }
}
