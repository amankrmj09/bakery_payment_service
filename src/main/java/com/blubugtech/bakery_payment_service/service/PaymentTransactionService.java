package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.dto.PaymentTransactionResponseDto;
import com.blubugtech.bakery_payment_service.entity.Payment;
import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.exception.PaymentServiceException;
import com.blubugtech.bakery_payment_service.repository.PaymentRepository;
import com.blubugtech.bakery_payment_service.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionService.class);

    final private PaymentTransactionRepository paymentTransactionRepository;

    final private PaymentRepository paymentRepository;

    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository, PaymentRepository paymentRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentRepository = paymentRepository;
    }

    // Create transaction
    public PaymentTransactionResponseDto createTransaction(UUID paymentId, PaymentTransaction.TransactionType transactionType,
                                                      BigDecimal amount, String description) {
        logger.info("Creating transaction for payment: {} type: {} amount: {}", paymentId, transactionType, amount);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Payment not found with ID: " + paymentId));

        PaymentTransaction transaction = new PaymentTransaction(payment, transactionType, amount, description);

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
        logger.info("Transaction created: {} for payment: {}", savedTransaction.getId(), paymentId);

        return PaymentTransactionResponseDto.from(savedTransaction);
    }

    // Get transaction by ID
    @Transactional(readOnly = true)
    public PaymentTransactionResponseDto getTransactionById(UUID transactionId) {
        logger.debug("Fetching transaction by ID: {}", transactionId);

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found with ID: " + transactionId));

        return PaymentTransactionResponseDto.from(transaction);
    }

    // Get transactions by payment ID
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto> getTransactionsByPaymentId(UUID paymentId) {
        logger.debug("Fetching transactions for payment: {}", paymentId);

        return paymentTransactionRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId).stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    // Get transactions by status
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto> getTransactionsByStatus(PaymentTransaction.TransactionStatus status) {
        logger.debug("Fetching transactions by status: {}", status);

        return paymentTransactionRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    // Get transactions by type
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto> getTransactionsByType(PaymentTransaction.TransactionType transactionType) {
        logger.debug("Fetching transactions by type: {}", transactionType);

        return paymentTransactionRepository.findByTransactionTypeOrderByCreatedAtDesc(transactionType).stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    // Get transaction by gateway transaction ID
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionResponseDto> getTransactionByGatewayId(String gatewayTransactionId) {
        logger.debug("Fetching transaction by gateway ID: {}", gatewayTransactionId);

        return paymentTransactionRepository.findByGatewayTransactionId(gatewayTransactionId)
                .map(PaymentTransactionResponseDto::from);
    }

    // Update transaction status
    public PaymentTransactionResponseDto updateTransactionStatus(UUID transactionId,
                                                           PaymentTransaction.TransactionStatus status,
                                                           String gatewayResponse) {
        logger.info("Updating transaction status: {} to {}", transactionId, status);

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found with ID: " + transactionId));

        transaction.setStatus(status);
        if (gatewayResponse != null) {
            transaction.setGatewayResponse(gatewayResponse);
        }

        if (status == PaymentTransaction.TransactionStatus.COMPLETED) {
            transaction.setProcessedAt(LocalDateTime.now());
        }

        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);
        logger.info("Transaction status updated: {}", transactionId);

        return PaymentTransactionResponseDto.from(updatedTransaction);
    }

    // Mark transaction as failed
    public PaymentTransactionResponseDto failTransaction(UUID transactionId, String failureReason, String failureCode) {
        logger.info("Failing transaction: {} reason: {}", transactionId, failureReason);

        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found with ID: " + transactionId));

        transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
        transaction.setFailureReason(failureReason);
        transaction.setFailureCode(failureCode);
        transaction.setGatewayResponse(failureReason);

        PaymentTransaction failedTransaction = paymentTransactionRepository.save(transaction);
        logger.info("Transaction failed: {}", transactionId);

        return PaymentTransactionResponseDto.from(failedTransaction);
    }

    // Get pending transactions
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto> getPendingTransactions() {
        logger.debug("Fetching pending transactions");

        return paymentTransactionRepository.findPendingTransactions().stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    // Get failed transactions
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto> getFailedTransactions() {
        logger.debug("Fetching failed transactions");

        return paymentTransactionRepository.findFailedTransactions().stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    // Get old pending transactions (for cleanup)
    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto> getOldPendingTransactions(int minutes) {
        logger.debug("Fetching pending transactions older than {} minutes", minutes);

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        return paymentTransactionRepository.findPendingTransactionsOlderThan(cutoffTime).stream()
                .map(PaymentTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    // Check if payment has successful transaction of specific type
    @Transactional(readOnly = true)
    public boolean hasSuccessfulTransaction(UUID paymentId, PaymentTransaction.TransactionType transactionType) {
        logger.debug("Checking if payment {} has successful transaction of type {}", paymentId, transactionType);

        return paymentTransactionRepository.hasSuccessfulTransaction(paymentId, transactionType);
    }

    // Get latest transaction by payment and type
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionResponseDto> getLatestTransactionByPaymentAndType(UUID paymentId,
                                                                                   PaymentTransaction.TransactionType transactionType) {
        logger.debug("Fetching latest transaction for payment {} and type {}", paymentId, transactionType);

        return paymentTransactionRepository.findLatestByPaymentAndType(paymentId, transactionType)
                .map(PaymentTransactionResponseDto::from);
    }

    // Get transaction statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching transaction statistics");

        try {
            List<Object[]> typeStats = paymentTransactionRepository.getTransactionStatisticsByType(startDate, endDate);
            List<Object[]> statusStats = paymentTransactionRepository.getTransactionStatisticsByStatus(startDate, endDate);

            long totalTransactions = paymentTransactionRepository.countByCreatedAtBetween(startDate, endDate);
            long completedTransactions = paymentTransactionRepository.countByStatusAndCreatedAtBetween(
                    PaymentTransaction.TransactionStatus.COMPLETED, startDate, endDate);
            long failedTransactions = paymentTransactionRepository.countByStatusAndCreatedAtBetween(
                    PaymentTransaction.TransactionStatus.FAILED, startDate, endDate);
            long pendingTransactions = paymentTransactionRepository.countByStatusAndCreatedAtBetween(
                    PaymentTransaction.TransactionStatus.PENDING, startDate, endDate);

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
            logger.error("Error fetching transaction statistics: {}", e.getMessage());
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
    long countByStatusAndCreatedAtBetween(PaymentTransaction.TransactionStatus status,
                                         LocalDateTime startDate, LocalDateTime endDate);
    */
}
