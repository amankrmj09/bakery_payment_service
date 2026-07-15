package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.entity.Refund;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);

    // Process payment through gateway
    public PaymentGatewayResponse processPayment(Payment payment) {
        logger.info("Processing payment through gateway: {} - {}",
                   payment.getPaymentGateway(), payment.getPaymentReference());

        return switch (payment.getPaymentGateway()) {
            case STRIPE -> processStripePayment(payment);
            case PAYPAL -> processPayPalPayment(payment);
            case SQUARE -> processSquarePayment(payment);
            case MOCK -> processMockPayment(payment);
            case MANUAL -> processManualPayment(payment);
        };
    }

    // Process refund through gateway
    public PaymentGatewayResponse processRefund(Refund refund) {
        logger.info("Processing refund through gateway: {} - {}",
                   refund.getPayment().getPaymentGateway(), refund.getRefundReference());

        return switch (refund.getPayment().getPaymentGateway()) {
            case STRIPE -> processStripeRefund(refund);
            case PAYPAL -> processPayPalRefund(refund);
            case SQUARE -> processSquareRefund(refund);
            case MOCK -> processMockRefund(refund);
            case MANUAL -> processManualRefund(refund);
        };
    }

    // Authorize payment (for two-step processing)
    public PaymentGatewayResponse authorizePayment(Payment payment) {
        logger.info("Authorizing payment: {}", payment.getPaymentReference());

        // Mock authorization
        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.AUTHORIZATION);
    }

    // Capture authorized payment
    public PaymentGatewayResponse capturePayment(Payment payment, BigDecimal amount) {
        logger.info("Capturing payment: {} amount: {}", payment.getPaymentReference(), amount);

        // Mock capture
        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.CAPTURE);
    }

    // Void authorized payment
    public PaymentGatewayResponse voidPayment(Payment payment) {
        logger.info("Voiding payment: {}", payment.getPaymentReference());

        // Mock void
        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.VOID);
    }

    // Private gateway implementations
    private PaymentGatewayResponse processStripePayment(Payment payment) {
        logger.debug("Processing Stripe payment: {}", payment.getPaymentReference());

        // Mock Stripe integration
        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.SALE);
    }

    private PaymentGatewayResponse processPayPalPayment(Payment payment) {
        logger.debug("Processing PayPal payment: {}", payment.getPaymentReference());

        // Mock PayPal integration
        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.SALE);
    }

    private PaymentGatewayResponse processSquarePayment(Payment payment) {
        logger.debug("Processing Square payment: {}", payment.getPaymentReference());

        // Mock Square integration
        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.SALE);
    }

    private PaymentGatewayResponse processMockPayment(Payment payment) {
        logger.debug("Processing Mock payment: {}", payment.getPaymentReference());

        return simulateGatewayResponse(payment, PaymentTransaction.TransactionType.SALE);
    }

    private PaymentGatewayResponse processManualPayment(Payment payment) {
        logger.debug("Processing Manual payment: {}", payment.getPaymentReference());

        // Manual payments are always pending until manually confirmed
        return PaymentGatewayResponse.builder()
                .success(false)
                .pending(true)
                .gatewayTransactionId(generateTransactionId())
                .gatewayResponse("Manual payment requires confirmation")
                .rawResponse("{\"status\":\"pending\",\"message\":\"Manual confirmation required\"}")
                .build();
    }

    private PaymentGatewayResponse processStripeRefund(Refund refund) {
        logger.debug("Processing Stripe refund: {}", refund.getRefundReference());

        return simulateRefundResponse(refund);
    }

    private PaymentGatewayResponse processPayPalRefund(Refund refund) {
        logger.debug("Processing PayPal refund: {}", refund.getRefundReference());

        return simulateRefundResponse(refund);
    }

    private PaymentGatewayResponse processSquareRefund(Refund refund) {
        logger.debug("Processing Square refund: {}", refund.getRefundReference());

        return simulateRefundResponse(refund);
    }

    private PaymentGatewayResponse processMockRefund(Refund refund) {
        logger.debug("Processing Mock refund: {}", refund.getRefundReference());

        return simulateRefundResponse(refund);
    }

    private PaymentGatewayResponse processManualRefund(Refund refund) {
        logger.debug("Processing Manual refund: {}", refund.getRefundReference());

        // Manual refunds are always pending
        return PaymentGatewayResponse.builder()
                .success(false)
                .pending(true)
                .gatewayTransactionId(generateTransactionId())
                .gatewayResponse("Manual refund requires confirmation")
                .rawResponse("{\"status\":\"pending\",\"message\":\"Manual confirmation required\"}")
                .build();
    }

    // Simulate gateway response
    private PaymentGatewayResponse simulateGatewayResponse(Payment payment, PaymentTransaction.TransactionType transactionType) {
        // Simulate random success/failure (90% success rate)
        boolean success = ThreadLocalRandom.current().nextDouble() < 0.90;

        if (success) {
            // Calculate gateway fee (2.9% + $0.30 for card payments)
            BigDecimal gatewayFee = BigDecimal.ZERO;
            if (payment.getPaymentMethod() == Payment.PaymentMethod.CARD) {
                gatewayFee = payment.getAmount().multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"));
            }

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .pending(false)
                    .gatewayTransactionId(generateTransactionId())
                    .gatewayResponse("Transaction approved")
                    .rawResponse(generateSuccessResponse(payment, transactionType))
                    .gatewayFee(gatewayFee)
                    .build();
        } else {
            String[] failureReasons = {
                "Insufficient funds",
                "Card declined",
                "Invalid card number",
                "Expired card",
                "Gateway timeout"
            };
            String failureReason = failureReasons[ThreadLocalRandom.current().nextInt(failureReasons.length)];

            return PaymentGatewayResponse.builder()
                    .success(false)
                    .pending(false)
                    .gatewayTransactionId(generateTransactionId())
                    .gatewayResponse(failureReason)
                    .rawResponse(generateFailureResponse(failureReason))
                    .failureCode("DECLINED")
                    .build();
        }
    }

    private PaymentGatewayResponse simulateRefundResponse(Refund refund) {
        // Simulate random success/failure (95% success rate for refunds)
        boolean success = ThreadLocalRandom.current().nextDouble() < 0.95;

        if (success) {
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .pending(false)
                    .gatewayTransactionId(generateTransactionId())
                    .gatewayResponse("Refund processed successfully")
                    .rawResponse(generateRefundSuccessResponse(refund))
                    .build();
        } else {
            String[] failureReasons = {
                "Original transaction not found",
                "Refund already processed",
                "Gateway timeout",
                "Invalid refund amount"
            };
            String failureReason = failureReasons[ThreadLocalRandom.current().nextInt(failureReasons.length)];

            return PaymentGatewayResponse.builder()
                    .success(false)
                    .pending(false)
                    .gatewayTransactionId(generateTransactionId())
                    .gatewayResponse(failureReason)
                    .rawResponse(generateRefundFailureResponse(failureReason))
                    .failureCode("REFUND_FAILED")
                    .build();
        }
    }

    private String generateTransactionId() {
        return "GW-" + System.currentTimeMillis() + "-" +
               ThreadLocalRandom.current().nextInt(10000, 99999);
    }

    private String generateSuccessResponse(Payment payment, PaymentTransaction.TransactionType transactionType) {
        return String.format(
            "{\"status\":\"success\",\"transaction_type\":\"%s\",\"amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
            transactionType.name().toLowerCase(),
            payment.getAmount().toString(),
            payment.getCurrencyCode(),
            LocalDateTime.now().toString()
        );
    }

    private String generateFailureResponse(String failureReason) {
        return String.format(
            "{\"status\":\"failed\",\"error\":\"%s\",\"timestamp\":\"%s\"}",
            failureReason,
            LocalDateTime.now().toString()
        );
    }

    private String generateRefundSuccessResponse(Refund refund) {
        return String.format(
            "{\"status\":\"success\",\"refund_amount\":\"%s\",\"currency\":\"%s\",\"timestamp\":\"%s\"}",
            refund.getAmount().toString(),
            refund.getCurrencyCode(),
            LocalDateTime.now().toString()
        );
    }

    private String generateRefundFailureResponse(String failureReason) {
        return String.format(
            "{\"status\":\"failed\",\"error\":\"%s\",\"timestamp\":\"%s\"}",
            failureReason,
            LocalDateTime.now().toString()
        );
    }

    // Response class
    @Getter
    public static class PaymentGatewayResponse {
        // Getters
        private final boolean success;
        private final boolean pending;
        private final String gatewayTransactionId;
        private final String gatewayResponse;
        private final String rawResponse;
        private final String failureCode;
        private final BigDecimal gatewayFee;

        private PaymentGatewayResponse(Builder builder) {
            this.success = builder.success;
            this.pending = builder.pending;
            this.gatewayTransactionId = builder.gatewayTransactionId;
            this.gatewayResponse = builder.gatewayResponse;
            this.rawResponse = builder.rawResponse;
            this.failureCode = builder.failureCode;
            this.gatewayFee = builder.gatewayFee;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean success;
            private boolean pending;
            private String gatewayTransactionId;
            private String gatewayResponse;
            private String rawResponse;
            private String failureCode;
            private BigDecimal gatewayFee = BigDecimal.ZERO;

            public Builder success(boolean success) { this.success = success; return this; }
            public Builder pending(boolean pending) { this.pending = pending; return this; }
            public Builder gatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; return this; }
            public Builder gatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; return this; }
            public Builder rawResponse(String rawResponse) { this.rawResponse = rawResponse; return this; }
            public Builder failureCode(String failureCode) { this.failureCode = failureCode; return this; }
            public Builder gatewayFee(BigDecimal gatewayFee) { this.gatewayFee = gatewayFee; return this; }

            public PaymentGatewayResponse build() {
                return new PaymentGatewayResponse(this);
            }
        }
    }
}
