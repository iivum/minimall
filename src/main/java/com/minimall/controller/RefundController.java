package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.RefundApplicationRequest;
import com.minimall.dto.RefundApprovalRequest;
import com.minimall.dto.RefundResponse;
import com.minimall.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Refund", description = "Refund management APIs")
public class RefundController {
    private final RefundService refundService;
    private final SecurityUtils securityUtils;

    public RefundController(RefundService refundService, SecurityUtils securityUtils) {
        this.refundService = refundService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/{orderId}/refund")
    @Operation(summary = "Apply for order refund")
    public ResponseEntity<RefundResponse> applyForRefund(
            @PathVariable String orderId,
            @Valid @RequestBody RefundApplicationRequest request) {
        String userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return ResponseEntity.ok(refundService.applyForRefund(orderId, userId, request));
    }

    @GetMapping("/{orderId}/refunds")
    @Operation(summary = "Get refunds for an order")
    public ResponseEntity<List<RefundResponse>> getOrderRefunds(@PathVariable String orderId) {
        return ResponseEntity.ok(refundService.getRefundsByOrder(orderId));
    }

    @GetMapping("/refunds/pending")
    @Operation(summary = "Get all pending refund requests (Admin only)")
    public ResponseEntity<List<RefundResponse>> getPendingRefunds() {
        return ResponseEntity.ok(refundService.getPendingRefunds());
    }

    @PostMapping("/refunds/{refundId}/approve")
    @Operation(summary = "Approve or reject a refund request (Admin only)")
    public ResponseEntity<RefundResponse> approveRefund(
            @PathVariable String refundId,
            @Valid @RequestBody RefundApprovalRequest request) {
        String userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return ResponseEntity.ok(refundService.approveRefund(refundId, userId, request));
    }

    @GetMapping("/refunds/{refundId}")
    @Operation(summary = "Get refund by ID")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable String refundId) {
        return ResponseEntity.ok(refundService.getRefundById(refundId));
    }
}