package com.blubugtech.bakery_payment_service.service;

import com.blubugtech.bakery_payment_service.enums.RefundStatus;

import com.blubugtech.bakery_payment_service.dto.payment.*;
import com.blubugtech.bakery_payment_service.dto.refund.*;
import com.blubugtech.bakery_payment_service.dto.transaction.*;
import com.blubugtech.bakery_payment_service.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public interface RefundService {
    RefundResponse createRefund(RefundRequest request);
    RefundResponse getRefundById(UUID refundId);
    RefundResponse getRefundByReference(String refundReference);
    List<RefundResponse> getRefundsByPaymentId(UUID paymentId);
    List<RefundResponse> getRefundsByStatus(RefundStatus status);
    Page<RefundResponse> getAllRefunds(Pageable pageable);
    List<RefundResponse> getRefundsByUser(UUID userId);
    RefundResponse approveRefund(UUID refundId, UUID approvedBy);
    RefundResponse rejectRefund(UUID refundId, String reason, UUID rejectedBy);
    List<RefundResponse> getPendingRefunds();
    List<RefundResponse> getCompletedRefunds();
    List<RefundResponse> getFailedRefunds();
    Map<String, Object> getRefundStatistics(LocalDateTime startDate, LocalDateTime endDate);
    List<RefundResponse> searchRefunds(String searchTerm);
}
