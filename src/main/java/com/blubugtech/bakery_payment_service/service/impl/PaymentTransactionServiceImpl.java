package com.blubugtech.bakery_payment_service.service.impl;

import com.blubugtech.bakery_payment_service.dto.transaction.PaymentTransactionResponse;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import com.blubugtech.bakery_payment_service.exception.payment.PaymentServiceException;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.repository.PaymentTransactionRepository;
import com.blubugtech.bakery_payment_service.service.PaymentTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import com.blubugtech.bakery_payment_service.mapper.PaymentTransactionMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    final private PaymentTransactionRepository paymentTransactionRepository;

    final private PaymentRepository paymentRepository;
    
    final private PaymentTransactionMapper paymentTransactionMapper;

    public PaymentTransactionServiceImpl(PaymentTransactionRepository paymentTransactionRepository, PaymentRepository paymentRepository, PaymentTransactionMapper paymentTransactionMapper) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.paymentTransactionMapper = paymentTransactionMapper;
    }

    // Create transaction
    public PaymentTransactionResponse createTransaction(UUID paymentId, TransactionType transactionType,
                                                        BigDecimal amount, String description) {
        log.info("Creating transaction for payment: {} type: {} amount: {}", paymentId, transactionType, amount);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + paymentId));

        PaymentTransaction transaction = new PaymentTransaction(payment, transactionType, amount, description);

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
        log.info("Transaction created: {} for payment: {}", savedTransaction.getId(), paymentId);

        return paymentTransactionMapper.toResponse(savedTransaction);
    }

    // Get transaction by ID
    @Transactional(readOnly = true)
    public PaymentTransactionResponse getTransactionById(UUID transactionId) {
        log.debug("Fetching transaction by ID: {}", transactionId);

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found with ID: " + transactionId));

        return paymentTransactionMapper.toResponse(transaction);
    }

    // Get transactions by payment ID
    @Transactional(readOnly = true)
    public PagedModel<PaymentTransactionResponse> getTransactionsByPaymentId(UUID paymentId, Pageable pageable) {
        log.debug("Fetching transactions for payment: {}", paymentId);

        Page<PaymentTransactionResponse> page = paymentTransactionRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId, pageable)
                .map(paymentTransactionMapper::toResponse);
        return new PagedModel<>(page);
    }

    // Get transactions by status
    @Transactional(readOnly = true)
    public PagedModel<PaymentTransactionResponse> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
        log.debug("Fetching transactions by status: {}", status);

        Page<PaymentTransactionResponse> page = paymentTransactionRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(paymentTransactionMapper::toResponse);
        return new PagedModel<>(page);
    }

    // Get transactions by type
    @Transactional(readOnly = true)
    public PagedModel<PaymentTransactionResponse> getTransactionsByType(TransactionType transactionType, Pageable pageable) {
        log.debug("Fetching transactions by type: {}", transactionType);

        Page<PaymentTransactionResponse> page = paymentTransactionRepository.findByTransactionTypeOrderByCreatedAtDesc(transactionType, pageable)
                .map(paymentTransactionMapper::toResponse);
        return new PagedModel<>(page);
    }

    // Get transaction by gateway transaction ID
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionResponse> getTransactionByGatewayId(String gatewayTransactionId) {
        log.debug("Fetching transaction by gateway ID: {}", gatewayTransactionId);

        return paymentTransactionRepository.findByGatewayTransactionId(gatewayTransactionId)
                .map(paymentTransactionMapper::toResponse);
    }

    // Update transaction status
    public PaymentTransactionResponse updateTransactionStatus(UUID transactionId,
                                                              TransactionStatus status,
                                                              String gatewayResponse) {
        log.info("Updating transaction status: {} to {}", transactionId, status);

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found with ID: " + transactionId));

        transaction.setStatus(status);
        if (gatewayResponse != null) {
            transaction.setGatewayResponse(gatewayResponse);
        }

        if (status == TransactionStatus.COMPLETED) {
            transaction.setProcessedAt(LocalDateTime.now());
        }

        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);
        log.info("Transaction status updated: {}", transactionId);

        return paymentTransactionMapper.toResponse(updatedTransaction);
    }

    // Mark transaction as failed
    public PaymentTransactionResponse failTransaction(UUID transactionId, String failureReason, String failureCode) {
        log.info("Failing transaction: {} reason: {}", transactionId, failureReason);

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found with ID: " + transactionId));

        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(failureReason);
        transaction.setFailureCode(failureCode);
        transaction.setGatewayResponse(failureReason);

        PaymentTransaction failedTransaction = paymentTransactionRepository.save(transaction);
        log.info("Transaction failed: {}", transactionId);

        return paymentTransactionMapper.toResponse(failedTransaction);
    }

    // Get pending transactions
    @Transactional(readOnly = true)
    public PagedModel<PaymentTransactionResponse> getPendingTransactions(Pageable pageable) {
        log.debug("Fetching pending transactions");

        Page<PaymentTransactionResponse> page = paymentTransactionRepository.findPendingTransactions(pageable)
                .map(paymentTransactionMapper::toResponse);
        return new PagedModel<>(page);
    }

    // Get failed transactions
    @Transactional(readOnly = true)
    public PagedModel<PaymentTransactionResponse> getFailedTransactions(Pageable pageable) {
        log.debug("Fetching failed transactions");

        Page<PaymentTransactionResponse> page = paymentTransactionRepository.findFailedTransactions(pageable)
                .map(paymentTransactionMapper::toResponse);
        return new PagedModel<>(page);
    }

    // Get old pending transactions (for cleanup)
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponse> getOldPendingTransactions(int minutes) {
        log.debug("Fetching pending transactions older than {} minutes", minutes);

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        return paymentTransactionRepository.findPendingTransactionsOlderThan(cutoffTime).stream()
                .map(PaymentTransactionResponse::from)
                .collect(Collectors.toList());
    }

    // Check if payment has successful transaction of specific type
    @Transactional(readOnly = true)
    public boolean hasSuccessfulTransaction(UUID paymentId, TransactionType transactionType) {
        log.debug("Checking if payment {} has successful transaction of type {}", paymentId, transactionType);

        return paymentTransactionRepository.hasSuccessfulTransaction(paymentId, transactionType);
    }

    // Get latest transaction by payment and type
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionResponse> getLatestTransactionByPaymentAndType(UUID paymentId,
                                                                                     TransactionType transactionType) {
        log.debug("Fetching latest transaction for payment {} and type {}", paymentId, transactionType);

        return paymentTransactionRepository.findLatestByPaymentAndType(paymentId, transactionType)
                .map(paymentTransactionMapper::toResponse);
    }

    // Get transaction statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching transaction statistics");

        try {
            List<Object[]> typeStats = paymentTransactionRepository.getTransactionStatisticsByType(startDate, endDate);
            List<Object[]> statusStats = paymentTransactionRepository.getTransactionStatisticsByStatus(startDate, endDate);

            long totalTransactions = paymentTransactionRepository.countByCreatedAtBetween(startDate, endDate);
            long completedTransactions = paymentTransactionRepository.countByStatusAndCreatedAtBetween(
                    TransactionStatus.COMPLETED, startDate, endDate);
            long failedTransactions = paymentTransactionRepository.countByStatusAndCreatedAtBetween(
                    TransactionStatus.FAILED, startDate, endDate);
            long pendingTransactions = paymentTransactionRepository.countByStatusAndCreatedAtBetween(
                    TransactionStatus.PENDING, startDate, endDate);

            return Map.of(
                    "totalTransactions", totalTransactions,
                    "completedTransactions", completedTransactions,
                    "failedTransactions", failedTransactions,
                    "pendingTransactions", pendingTransactions,
                    "transactionsByType", typeStats,
                    "transactionsByStatus", statusStats,
                    "dateRange", Map.of(
                            "startDate", startDate.toString(),
                            "endDate", endDate.toString()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching transaction statistics: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Transaction statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Add missing repository methods in PaymentTransactionRepository
    // (These should be added to PaymentTransactionRepository.java)
    /*
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByStatusAndCreatedAtBetween(TransactionStatus status,
                                         LocalDateTime startDate, LocalDateTime endDate);
    */
}
