package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.dto.transaction.PaymentTransactionResponse;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface PaymentTransactionService {
    PaymentTransactionResponse getTransactionById(UUID transactionId);

    PagedModel<PaymentTransactionResponse> getTransactionsByPaymentId(UUID paymentId, Pageable pageable);

    PagedModel<PaymentTransactionResponse> getTransactionsByStatus(TransactionStatus status, Pageable pageable);

    PagedModel<PaymentTransactionResponse> getTransactionsByType(TransactionType transactionType, Pageable pageable);

    Optional<PaymentTransactionResponse> getTransactionByGatewayId(String gatewayTransactionId);

    PaymentTransactionResponse failTransaction(UUID transactionId, String failureReason, String failureCode);

    PagedModel<PaymentTransactionResponse> getPendingTransactions(Pageable pageable);

    PagedModel<PaymentTransactionResponse> getFailedTransactions(Pageable pageable);

    List<PaymentTransactionResponse> getOldPendingTransactions(int minutes);

    boolean hasSuccessfulTransaction(UUID paymentId, TransactionType transactionType);

    Map<String, Object> getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
