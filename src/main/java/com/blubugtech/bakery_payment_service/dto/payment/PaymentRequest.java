package com.blubugtech.bakery_payment_service.dto.payment;

import com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider;
import com.blubugtech.bakery_payment_service.enums.PaymentMethod;

import com.blubugtech.bakery_payment_service.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {

    // Getters and Setters
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private PaymentGatewayProvider paymentGateway = PaymentGatewayProvider.MOCK;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = "USD";

    private String description;

    // Card payment details
    @Size(max = 4, message = "Card last four must be 4 digits")
    private String cardLastFour;

    @Size(max = 20, message = "Card brand must not exceed 20 characters")
    private String cardBrand;

    @Size(max = 20, message = "Card type must not exceed 20 characters")
    private String cardType;

    // Digital wallet details
    @Size(max = 50, message = "Wallet provider must not exceed 50 characters")
    private String digitalWalletProvider;

    // Bank transfer details
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    // Gateway specific fields
    @Size(max = 100, message = "External transaction ID must not exceed 100 characters")
    private String externalTransactionId;

    private String notes;

    private Map<String, Object> metadata;

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(UUID orderId, UUID userId, PaymentMethod paymentMethod, BigDecimal amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

}
