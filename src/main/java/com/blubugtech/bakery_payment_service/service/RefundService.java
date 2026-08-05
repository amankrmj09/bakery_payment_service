package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.dto.refund.RefundRequest;
import com.blubugtech.bakery_payment_service.dto.refund.RefundResponse;
import com.blubugtech.bakery_payment_service.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RefundService {
    RefundResponse createRefund(RefundRequest request);

    RefundResponse getRefundById(UUID refundId);

    RefundResponse getRefundByReference(String refundReference);

    PagedModel<RefundResponse> getRefundsByPaymentId(UUID paymentId, Pageable pageable);

    PagedModel<RefundResponse> getRefundsByStatus(RefundStatus status, Pageable pageable);

    PagedModel<RefundResponse> getAllRefunds(Pageable pageable);

    PagedModel<RefundResponse> getRefundsByUser(UUID userId, Pageable pageable);

    RefundResponse approveRefund(UUID refundId, UUID approvedBy);

    RefundResponse rejectRefund(UUID refundId, String reason, UUID rejectedBy);

    PagedModel<RefundResponse> getPendingRefunds(Pageable pageable);

    PagedModel<RefundResponse> getCompletedRefunds(Pageable pageable);

    PagedModel<RefundResponse> getFailedRefunds(Pageable pageable);

    Map<String, Object> getRefundStatistics(LocalDateTime startDate, LocalDateTime endDate);

    PagedModel<RefundResponse> searchRefunds(String searchTerm, Pageable pageable);

    PagedModel<RefundResponse> getRefundsWithFilters(RefundStatus status, UUID requestedBy,
                                                     UUID approvedBy, java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount,
                                                     LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
