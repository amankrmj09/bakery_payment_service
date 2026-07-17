package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;

import com.blubugtech.bakery_payment_service.dto.payment.*;
import com.blubugtech.bakery_payment_service.dto.refund.*;
import com.blubugtech.bakery_payment_service.dto.transaction.*;
import com.blubugtech.bakery_payment_service.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public interface PaymentTransactionService {
    PaymentTransactionResponse getTransactionById(UUID transactionId);
    List<PaymentTransactionResponse> getTransactionsByPaymentId(UUID paymentId);
    List<PaymentTransactionResponse> getTransactionsByStatus(TransactionStatus status);
    List<PaymentTransactionResponse> getTransactionsByType(TransactionType transactionType);
    Optional<PaymentTransactionResponse> getTransactionByGatewayId(String gatewayTransactionId);
    PaymentTransactionResponse failTransaction(UUID transactionId, String failureReason, String failureCode);
    List<PaymentTransactionResponse> getPendingTransactions();
    List<PaymentTransactionResponse> getFailedTransactions();
    List<PaymentTransactionResponse> getOldPendingTransactions(int minutes);
    boolean hasSuccessfulTransaction(UUID paymentId, TransactionType transactionType);
    Map<String, Object> getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
