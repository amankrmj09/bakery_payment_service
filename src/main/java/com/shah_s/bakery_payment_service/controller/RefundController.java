package com.shah_s.bakery_payment_service.controller;

import com.shah_s.bakery_payment_service.dto.RefundRequestDto;
import com.shah_s.bakery_payment_service.dto.RefundResponseDto;
import com.shah_s.bakery_payment_service.entity.Refund;
import com.shah_s.bakery_payment_service.service.RefundService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/refunds")

public class RefundController {

    private static final Logger logger = LoggerFactory.getLogger(RefundController.class);

    final private RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    // Create refund
    @PostMapping
    public ResponseEntity<RefundResponseDto> createRefund(
            @Valid @RequestBody RefundRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Create refund request received for payment: {}", request.getPaymentId());

        // Use header userId if available (from Gateway)
        if (userId != null) {
            request.setRequestedBy(userId);
        }

        RefundResponseDto refund = refundService.createRefund(request);

        logger.info("Refund created successfully: {}", refund.getRefundReference());
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }

    // Get all refunds with pagination
    @GetMapping
    public ResponseEntity<Page<RefundResponseDto>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get all refunds request received (page: {}, size: {})", page, size);

        // Only admins can view all refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RefundResponseDto> refunds = refundService.getAllRefunds(pageable);

        logger.info("Retrieved {} refunds (page {} of {})", refunds.getContent().size(),
                   page + 1, refunds.getTotalPages());
        return ResponseEntity.ok(refunds);
    }

    // Get refund by ID
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponseDto> getRefundById(
            @PathVariable UUID refundId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get refund by ID request received: {}", refundId);

        RefundResponseDto refund = refundService.getRefundById(refundId);

        // Check if user can access this refund (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !refund.getRequestedBy().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Refund retrieved: {}", refund.getRefundReference());
        return ResponseEntity.ok(refund);
    }

    // Get refund by reference
    @GetMapping("/reference/{refundReference}")
    public ResponseEntity<RefundResponseDto> getRefundByReference(
            @PathVariable String refundReference,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get refund by reference request received: {}", refundReference);

        RefundResponseDto refund = refundService.getRefundByReference(refundReference);

        // Check if user can access this refund (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !refund.getRequestedBy().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Refund found: {}", refundReference);
        return ResponseEntity.ok(refund);
    }

    // Get refunds by payment ID
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByPaymentId(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get refunds by payment ID request received: {}", paymentId);

        List<RefundResponseDto> refunds = refundService.getRefundsByPaymentId(paymentId);

        // Check if user can access these refunds (unless admin)
        if (userId != null && !"ADMIN".equals(userRole)) {
            refunds = refunds.stream()
                    .filter(refund -> refund.getRequestedBy().equals(userId))
                    .toList();
        }

        logger.info("Retrieved {} refunds for payment", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Get refunds by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByUser(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get refunds by user ID request received: {}", userId);

        // Check if user can access these refunds (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.getRefundsByUser(userId);

        logger.info("Retrieved {} refunds for user", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Get refunds by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByStatus(
            @PathVariable Refund.RefundStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get refunds by status request received: {}", status);

        // Only admins can view refunds by status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.getRefundsByStatus(status);

        logger.info("Retrieved {} refunds with status {}", refunds.size(), status);
        return ResponseEntity.ok(refunds);
    }

    // Approve refund
    @PostMapping("/{refundId}/approve")
    public ResponseEntity<RefundResponseDto> approveRefund(
            @PathVariable UUID refundId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Approve refund request received: {}", refundId);

        // Only admins can approve refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RefundResponseDto refund = refundService.approveRefund(refundId, userId);

        logger.info("Refund approved successfully: {}", refundId);
        return ResponseEntity.ok(refund);
    }

    // Reject refund
    @PostMapping("/{refundId}/reject")
    public ResponseEntity<RefundResponseDto> rejectRefund(
            @PathVariable UUID refundId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Reject refund request received: {}", refundId);

        // Only admins can reject refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String reason = request.get("reason");
        RefundResponseDto refund = refundService.rejectRefund(refundId, reason, userId);

        logger.info("Refund rejected successfully: {}", refundId);
        return ResponseEntity.ok(refund);
    }

    // Get pending refunds
    @GetMapping("/pending")
    public ResponseEntity<List<RefundResponseDto>> getPendingRefunds(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get pending refunds request received");

        // Only admins can view pending refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.getPendingRefunds();

        logger.info("Retrieved {} pending refunds", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Get completed refunds
    @GetMapping("/completed")
    public ResponseEntity<List<RefundResponseDto>> getCompletedRefunds(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get completed refunds request received");

        // Only admins can view completed refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.getCompletedRefunds();

        logger.info("Retrieved {} completed refunds", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Get failed refunds
    @GetMapping("/failed")
    public ResponseEntity<List<RefundResponseDto>> getFailedRefunds(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get failed refunds request received");

        // Only admins can view failed refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.getFailedRefunds();

        logger.info("Retrieved {} failed refunds", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Search refunds
    @GetMapping("/search")
    public ResponseEntity<List<RefundResponseDto>> searchRefunds(
            @RequestParam String query,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Search refunds request received with query: {}", query);

        // Only admins can search all refunds
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.searchRefunds(query);

        logger.info("Search returned {} refunds", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Advanced search with filters
    @GetMapping("/filter")
    public ResponseEntity<List<RefundResponseDto>> getRefundsWithFilters(
            @RequestParam(required = false) Refund.RefundStatus status,
            @RequestParam(required = false) UUID requestedBy,
            @RequestParam(required = false) UUID approvedBy,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Advanced filter search request received");

        // Only admins can use advanced filters
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<RefundResponseDto> refunds = refundService.getRefundsWithFilters(
                status, requestedBy, approvedBy, minAmount, maxAmount, startDate, endDate);

        logger.info("Filter search returned {} refunds", refunds.size());
        return ResponseEntity.ok(refunds);
    }

    // Get refund statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRefundStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get refund statistics request received");

        // Only admins can view statistics
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = refundService.getRefundStatistics(startDate, endDate);

        logger.info("Refund statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service-refunds");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}
