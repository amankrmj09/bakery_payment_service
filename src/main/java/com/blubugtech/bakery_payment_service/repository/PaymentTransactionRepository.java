package com.blubugtech.bakery_payment_service.repository;

import com.blubugtech.bakery_payment_service.entity.PaymentTransaction;
import com.blubugtech.bakery_payment_service.enums.TransactionStatus;
import com.blubugtech.bakery_payment_service.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    // Find transactions by payment ID
    Page<PaymentTransaction> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId, Pageable pageable);

    // Find transactions by transaction type
    Page<PaymentTransaction> findByTransactionTypeOrderByCreatedAtDesc(TransactionType transactionType, Pageable pageable);

    // Find transactions by status
    Page<PaymentTransaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status, Pageable pageable);

    // Find transaction by gateway transaction ID
    Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId);

    // Find transactions by payment and transaction type
    List<PaymentTransaction> findByPaymentIdAndTransactionTypeOrderByCreatedAtDesc(UUID paymentId,
                                                                                   TransactionType transactionType);

    // Find transactions by payment and status
    List<PaymentTransaction> findByPaymentIdAndStatusOrderByCreatedAtDesc(UUID paymentId,
                                                                          TransactionStatus status);

    // Find transactions by date range
    List<PaymentTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find failed transactions
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'FAILED' ORDER BY pt.createdAt DESC")
    Page<PaymentTransaction> findFailedTransactions(Pageable pageable);

    // Find pending transactions
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' ORDER BY pt.createdAt ASC")
    Page<PaymentTransaction> findPendingTransactions(Pageable pageable);

    // Find transactions pending for too long
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' AND pt.createdAt <= :cutoffTime ORDER BY pt.createdAt ASC")
    List<PaymentTransaction> findPendingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Count transactions by type
    long countByTransactionType(TransactionType transactionType);

    // Count transactions by status
    long countByStatus(TransactionStatus status);

    // Count transactions by payment
    long countByPaymentId(UUID paymentId);

    // Count transactions by createdAt between two dates
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count transactions by status and createdAt between two dates
    long countByStatusAndCreatedAtBetween(TransactionStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Get total amount by transaction type
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PaymentTransaction pt WHERE pt.transactionType = :transactionType")
    BigDecimal getTotalAmountByTransactionType(@Param("transactionType") TransactionType transactionType);

    // Get transaction statistics by type
    @Query("SELECT pt.transactionType as transactionType, " +
            "COUNT(pt) as transactionCount, " +
            "SUM(pt.amount) as totalAmount, " +
            "COUNT(CASE WHEN pt.status = 'COMPLETED' THEN 1 END) as successfulTransactions, " +
            "COUNT(CASE WHEN pt.status = 'FAILED' THEN 1 END) as failedTransactions " +
            "FROM PaymentTransaction pt " +
            "WHERE pt.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY pt.transactionType " +
            "ORDER BY COUNT(pt) DESC")
    List<Object[]> getTransactionStatisticsByType(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Get transaction statistics by status
    @Query("SELECT pt.status as status, " +
            "COUNT(pt) as transactionCount, " +
            "SUM(pt.amount) as totalAmount " +
            "FROM PaymentTransaction pt " +
            "WHERE pt.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY pt.status")
    List<Object[]> getTransactionStatisticsByStatus(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Get latest transaction by payment and type
    @Query("SELECT pt FROM PaymentTransaction pt " +
            "WHERE pt.payment.id = :paymentId AND pt.transactionType = :transactionType " +
            "ORDER BY pt.createdAt DESC LIMIT 1")
    Optional<PaymentTransaction> findLatestByPaymentAndType(@Param("paymentId") UUID paymentId,
                                                            @Param("transactionType") TransactionType transactionType);

    // Check if payment has successful transaction of specific type
    @Query("SELECT COUNT(pt) > 0 FROM PaymentTransaction pt " +
            "WHERE pt.payment.id = :paymentId " +
            "AND pt.transactionType = :transactionType " +
            "AND pt.status = 'COMPLETED'")
    boolean hasSuccessfulTransaction(@Param("paymentId") UUID paymentId,
                                     @Param("transactionType") TransactionType transactionType);
}
