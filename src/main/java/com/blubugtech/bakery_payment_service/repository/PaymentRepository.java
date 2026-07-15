package com.blubugtech.bakery_payment_service.repository;

import com.blubugtech.bakery_payment_service.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Find payment by payment reference
    Optional<Payment> findByPaymentReference(String paymentReference);

    // Check if payment reference exists
    boolean existsByPaymentReference(String paymentReference);

    // Find payment by order ID
    Optional<Payment> findByOrderId(UUID orderId);

    // Find payments by user ID
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Find payments by user ID with pagination
    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    // Find payment by external transaction ID
    Optional<Payment> findByExternalTransactionId(String externalTransactionId);

    // Find payment by gateway payment ID
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    // Find payments by status
    List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);

    // Find payments by status with pagination
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    // Find payments by multiple statuses
    @Query("SELECT p FROM Payment p WHERE p.status IN :statuses ORDER BY p.createdAt DESC")
    List<Payment> findByStatusIn(@Param("statuses") List<Payment.PaymentStatus> statuses);

    // Find payments by payment method
    List<Payment> findByPaymentMethodOrderByCreatedAtDesc(Payment.PaymentMethod paymentMethod);

    // Find payments by payment gateway
    List<Payment> findByPaymentGatewayOrderByCreatedAtDesc(Payment.PaymentGateway paymentGateway);

    // Find payments by payment method and status
    List<Payment> findByPaymentMethodAndStatusOrderByCreatedAtDesc(Payment.PaymentMethod paymentMethod,
                                                                   Payment.PaymentStatus status);

    // Find payments by date range
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find payments by date range with pagination
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find payments by user and status
    List<Payment> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, Payment.PaymentStatus status);

    // Find payments by user and date range
    List<Payment> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID userId, LocalDateTime startDate,
                                                                      LocalDateTime endDate);

    // Find payments by amount range
    List<Payment> findByAmountBetweenOrderByCreatedAtDesc(BigDecimal minAmount, BigDecimal maxAmount);

    // Find expired payments
    @Query("SELECT p FROM Payment p WHERE p.expiresAt < :currentTime AND p.status = 'PENDING' ORDER BY p.expiresAt ASC")
    List<Payment> findExpiredPayments(@Param("currentTime") LocalDateTime currentTime);

    // Find payments pending for too long
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt <= :cutoffTime ORDER BY p.createdAt ASC")
    List<Payment> findPendingPaymentsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find failed payments that can be retried
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < 3 ORDER BY p.lastRetryAt ASC")
    List<Payment> findRetriableFailedPayments();

    // Find payments by card details
    List<Payment> findByCardLastFourAndCardBrandOrderByCreatedAtDesc(String cardLastFour, String cardBrand);

    // Find payments by digital wallet provider
    List<Payment> findByDigitalWalletProviderOrderByCreatedAtDesc(String digitalWalletProvider);

    // Find payments by bank name
    List<Payment> findByBankNameOrderByCreatedAtDesc(String bankName);

    // Find refundable payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' ORDER BY p.capturedAt DESC")
    List<Payment> findRefundablePayments();

    // Find payments with refunds
    @Query("SELECT DISTINCT p FROM Payment p JOIN p.refunds r ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsWithRefunds();

    // Count payments by status
    long countByStatus(Payment.PaymentStatus status);

    // Count payments by payment method
    long countByPaymentMethod(Payment.PaymentMethod paymentMethod);

    // Count payments by payment gateway
    long countByPaymentGateway(Payment.PaymentGateway paymentGateway);

    // Count payments by user
    long countByUserId(UUID userId);

    // Count payments by date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count payments by status and date range
    long countByStatusAndCreatedAtBetween(Payment.PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Get total amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") Payment.PaymentStatus status);

    // Get total amount by payment method
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = :paymentMethod")
    BigDecimal getTotalAmountByPaymentMethod(@Param("paymentMethod") Payment.PaymentMethod paymentMethod);

    // Get total amount by date range
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Get total gateway fees
    @Query("SELECT COALESCE(SUM(p.gatewayFee), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalGatewayFees(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Get net amount (after fees)
    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalNetAmount(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    // Get payment statistics by method
    @Query("SELECT p.paymentMethod as paymentMethod, " +
           "COUNT(p) as paymentCount, " +
           "SUM(p.amount) as totalAmount, " +
           "AVG(p.amount) as averageAmount, " +
           "SUM(p.gatewayFee) as totalFees " +
           "FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.paymentMethod " +
           "ORDER BY SUM(p.amount) DESC")
    List<Object[]> getPaymentStatisticsByMethod(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Get payment statistics by gateway
    @Query("SELECT p.paymentGateway as paymentGateway, " +
           "COUNT(p) as paymentCount, " +
           "SUM(p.amount) as totalAmount, " +
           "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as successfulPayments, " +
           "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPayments " +
           "FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.paymentGateway " +
           "ORDER BY SUM(p.amount) DESC")
    List<Object[]> getPaymentStatisticsByGateway(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Get payment statistics by status
    @Query("SELECT p.status as status, " +
           "COUNT(p) as paymentCount, " +
           "SUM(p.amount) as totalAmount " +
           "FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.status")
    List<Object[]> getPaymentStatisticsByStatus(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Get daily payment statistics
    @Query(value = "SELECT DATE(p.created_at) as payment_date, " +
                   "COUNT(p) as payment_count, " +
                   "SUM(p.amount) as total_amount, " +
                   "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as successful_payments, " +
                   "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failed_payments, " +
                   "AVG(p.amount) as average_amount " +
                   "FROM payments p " +
                   "WHERE p.created_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE(p.created_at) " +
                   "ORDER BY DATE(p.created_at) DESC", nativeQuery = true)
    List<Object[]> getDailyPaymentStatistics(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Get payment success rate
    @Query("SELECT " +
           "COUNT(p) as totalPayments, " +
           "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as successfulPayments, " +
           "COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failedPayments, " +
           "COUNT(CASE WHEN p.status = 'PENDING' THEN 1 END) as pendingPayments " +
           "FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Object[] getPaymentSuccessRate(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Get average processing time
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (p.captured_at - p.created_at))/60) " +
                   "FROM payments p " +
                   "WHERE p.status = 'COMPLETED' " +
                   "AND p.captured_at IS NOT NULL " +
                   "AND p.created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Double getAverageProcessingTimeInMinutes(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Get top payment methods by usage
    @Query("SELECT p.paymentMethod, COUNT(p) as usageCount " +
           "FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.paymentMethod " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getTopPaymentMethods(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Get top users by payment volume
    @Query("SELECT p.userId, " +
           "COUNT(p) as paymentCount, " +
           "SUM(p.amount) as totalAmount " +
           "FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "AND p.status = 'COMPLETED' " +
           "GROUP BY p.userId " +
           "ORDER BY SUM(p.amount) DESC")
    List<Object[]> getTopUsersByPaymentVolume(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    // Advanced search with multiple filters
    @Query("SELECT p FROM Payment p " +
           "WHERE (:userId IS NULL OR p.userId = :userId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) " +
           "AND (:paymentGateway IS NULL OR p.paymentGateway = :paymentGateway) " +
           "AND (:minAmount IS NULL OR p.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR p.amount <= :maxAmount) " +
           "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR p.createdAt <= :endDate) " +
           "ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsWithFilters(@Param("userId") UUID userId,
                                         @Param("status") Payment.PaymentStatus status,
                                         @Param("paymentMethod") Payment.PaymentMethod paymentMethod,
                                         @Param("paymentGateway") Payment.PaymentGateway paymentGateway,
                                         @Param("minAmount") BigDecimal minAmount,
                                         @Param("maxAmount") BigDecimal maxAmount,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Search payments by description or notes
    @Query("SELECT p FROM Payment p " +
           "WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<Payment> searchPaymentsByText(@Param("searchTerm") String searchTerm);
}
