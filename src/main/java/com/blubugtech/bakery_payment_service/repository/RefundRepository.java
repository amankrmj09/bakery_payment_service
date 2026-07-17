package com.blubugtech.bakery_payment_service.repository;

import com.blubugtech.bakery_payment_service.enums.RefundStatus;

import com.blubugtech.bakery_payment_service.entity.Refund;
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
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    // Find refund by refund reference
    Optional<Refund> findByRefundReference(String refundReference);

    // Check if refund reference exists
    boolean existsByRefundReference(String refundReference);

    // Find refund by gateway refund ID
    Optional<Refund> findByGatewayRefundId(String gatewayRefundId);

    // Find refunds by payment ID
    List<Refund> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

    // Find refunds by status
    List<Refund> findByStatusOrderByCreatedAtDesc(RefundStatus status);

    // Find refunds by status with pagination
    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);

    // Find refunds by requested by user
    List<Refund> findByRequestedByOrderByCreatedAtDesc(UUID requestedBy);

    // Find refunds by approved by user
    List<Refund> findByApprovedByOrderByCreatedAtDesc(UUID approvedBy);

    // Find refunds by date range
    List<Refund> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find refunds by date range with pagination
    Page<Refund> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find refunds by amount range
    List<Refund> findByAmountBetweenOrderByCreatedAtDesc(BigDecimal minAmount, BigDecimal maxAmount);

    // Find pending refunds
    @Query("SELECT r FROM Refund r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Refund> findPendingRefunds();

    // Find pending refunds older than specific time
    @Query("SELECT r FROM Refund r WHERE r.status = 'PENDING' AND r.createdAt <= :cutoffTime ORDER BY r.createdAt ASC")
    List<Refund> findPendingRefundsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find processing refunds
    @Query("SELECT r FROM Refund r WHERE r.status = 'PROCESSING' ORDER BY r.createdAt ASC")
    List<Refund> findProcessingRefunds();

    // Find failed refunds
    @Query("SELECT r FROM Refund r WHERE r.status = 'FAILED' ORDER BY r.createdAt DESC")
    List<Refund> findFailedRefunds();

    // Find completed refunds
    @Query("SELECT r FROM Refund r WHERE r.status = 'COMPLETED' ORDER BY r.completedAt DESC")
    List<Refund> findCompletedRefunds();

    // Find refunds by payment and status
    List<Refund> findByPaymentIdAndStatusOrderByCreatedAtDesc(UUID paymentId, RefundStatus status);

    // Count refunds by status
    long countByStatus(RefundStatus status);

    // Count refunds by payment
    long countByPaymentId(UUID paymentId);

    // Count refunds by requested by user
    long countByRequestedBy(UUID requestedBy);

    // Count refunds by date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count refunds by status and date range
    long countByStatusAndCreatedAtBetween(RefundStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Get total refund amount by status
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.status = :status")
    BigDecimal getTotalRefundAmountByStatus(@Param("status") RefundStatus status);

    // Get total refund amount by date range
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRefundAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Get total refund amount for payment
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = 'COMPLETED'")
    BigDecimal getTotalRefundAmountForPayment(@Param("paymentId") UUID paymentId);

    // Get refund statistics by status
    @Query("SELECT r.status as status, " +
           "COUNT(r) as refundCount, " +
           "SUM(r.amount) as totalAmount, " +
           "AVG(r.amount) as averageAmount " +
           "FROM Refund r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY r.status")
    List<Object[]> getRefundStatisticsByStatus(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Get daily refund statistics
    @Query(value = "SELECT DATE(r.created_at) as refund_date, " +
                   "COUNT(r) as refund_count, " +
                   "SUM(r.amount) as total_amount, " +
                   "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) as successful_refunds, " +
                   "COUNT(CASE WHEN r.status = 'FAILED' THEN 1 END) as failed_refunds " +
                   "FROM refunds r " +
                   "WHERE r.created_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE(r.created_at) " +
                   "ORDER BY DATE(r.created_at) DESC", nativeQuery = true)
    List<Object[]> getDailyRefundStatistics(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Get refund success rate
    @Query("SELECT " +
           "COUNT(r) as totalRefunds, " +
           "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) as successfulRefunds, " +
           "COUNT(CASE WHEN r.status = 'FAILED' THEN 1 END) as failedRefunds, " +
           "COUNT(CASE WHEN r.status = 'PENDING' THEN 1 END) as pendingRefunds " +
           "FROM Refund r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate")
    Object[] getRefundSuccessRate(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Get average refund processing time
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (r.completed_at - r.created_at))/60) " +
                   "FROM refunds r " +
                   "WHERE r.status = 'COMPLETED' " +
                   "AND r.completed_at IS NOT NULL " +
                   "AND r.created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Double getAverageRefundProcessingTimeInMinutes(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Get top users by refund amount
    @Query("SELECT r.requestedBy, " +
           "COUNT(r) as refundCount, " +
           "SUM(r.amount) as totalRefundAmount " +
           "FROM Refund r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.status = 'COMPLETED' " +
           "GROUP BY r.requestedBy " +
           "ORDER BY SUM(r.amount) DESC")
    List<Object[]> getTopUsersByRefundAmount(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    // Search refunds by reason or notes
    @Query("SELECT r FROM Refund r " +
           "WHERE LOWER(r.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(r.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY r.createdAt DESC")
    List<Refund> searchRefundsByText(@Param("searchTerm") String searchTerm);

    // Advanced search with multiple filters
    @Query("SELECT r FROM Refund r " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:requestedBy IS NULL OR r.requestedBy = :requestedBy) " +
           "AND (:approvedBy IS NULL OR r.approvedBy = :approvedBy) " +
           "AND (:minAmount IS NULL OR r.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR r.amount <= :maxAmount) " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<Refund> findRefundsWithFilters(@Param("status") RefundStatus status,
                                       @Param("requestedBy") UUID requestedBy,
                                       @Param("approvedBy") UUID approvedBy,
                                       @Param("minAmount") BigDecimal minAmount,
                                       @Param("maxAmount") BigDecimal maxAmount,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
