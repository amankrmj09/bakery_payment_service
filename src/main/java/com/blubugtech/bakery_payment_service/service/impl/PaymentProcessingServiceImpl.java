package com.blubugtech.bakery_payment_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import com.blubugtech.bakery_payment_service.enums.PaymentStatus;
import com.blubugtech.bakery_payment_service.service.PaymentProcessingService;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGatewayResult;
import com.blubugtech.bakery_payment_service.integration.payment.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final List<PaymentGateway> paymentGateways;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PaymentProcessingServiceImpl(PaymentRepository paymentRepository, List<PaymentGateway> paymentGateways, ApplicationEventPublisher applicationEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentGateways = paymentGateways;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async
    @Override
    public void processPaymentAsync(Payment payment) {
        log.info("Processing payment asynchronously: {}", payment.getPaymentReference());

        try {
            // Update status to processing
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            // Process through gateway
            PaymentGatewayResult gatewayResponse = getGatewayForMethod(payment.getPaymentMethod()).processPayment(payment, TransactionType.SALE);

            // Create transaction record
            PaymentTransaction transaction = new PaymentTransaction(payment,
                    TransactionType.SALE, payment.getAmount(),
                    "Payment processing");

            // Update payment based on gateway response
            if (gatewayResponse.isSuccess()) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCapturedAt(LocalDateTime.now());
                payment.setGatewayFee(gatewayResponse.getGatewayFee());
                payment.calculateNetAmount();
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setProcessedAt(LocalDateTime.now());
            } else if (gatewayResponse.isPending()) {
                payment.setStatus(PaymentStatus.PENDING);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(LocalDateTime.now());
                payment.setFailureReason(gatewayResponse.getGatewayResponse());
                payment.setFailureCode(gatewayResponse.getFailureCode());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason(gatewayResponse.getGatewayResponse());
                transaction.setFailureCode(gatewayResponse.getFailureCode());
            }

            // Update gateway information
            payment.setGatewayPaymentId(gatewayResponse.getGatewayTransactionId());
            payment.setGatewayResponse(gatewayResponse.getGatewayResponse());
            payment.setGatewayRawResponse(gatewayResponse.getRawResponse());

            transaction.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
            transaction.setGatewayResponse(gatewayResponse.getGatewayResponse());
            transaction.setGatewayRawResponse(gatewayResponse.getRawResponse());

            // Save payment and transaction
            payment.addTransaction(transaction);
            paymentRepository.save(payment);

            // Notify order service
            applicationEventPublisher.publishEvent(new com.blubugtech.bakery_payment_service.event.PaymentStatusUpdatedApplicationEvent(this, payment));

            log.info("Payment processing completed: {} status: {}",
                       payment.getPaymentReference(), payment.getStatus());

        } catch (Exception e) {
            log.error("Payment processing failed: {} - {}", payment.getPaymentReference(), e.getMessage(), e);

            // Update payment as failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailedAt(LocalDateTime.now());
            payment.setFailureReason("Payment processing error: " + e.getMessage());
            paymentRepository.save(payment);

            // Notify order service
            applicationEventPublisher.publishEvent(new com.blubugtech.bakery_payment_service.event.PaymentStatusUpdatedApplicationEvent(this, payment));
        }
    }

    private PaymentGateway getGatewayForMethod(com.blubugtech.bakery_payment_service.enums.PaymentMethod method) {
        com.blubugtech.bakery_payment_service.enums.PaymentGatewayProvider provider = method.getDefaultProvider();
        return paymentGateways.stream()
                .filter(g -> g.getProviderType() == provider)
                .findFirst()
                .orElseThrow(() -> new com.blubugtech.bakery_payment_service.exception.payment.PaymentServiceException("No gateway available for provider: " + provider));
    }
}
