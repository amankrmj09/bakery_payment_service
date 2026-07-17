package com.blubugtech.bakery_payment_service.enums;

public enum TransactionType {
    AUTHORIZATION,
    CAPTURE,
    SALE,  // Combined auth + capture
    VOID,
    REFUND
}
