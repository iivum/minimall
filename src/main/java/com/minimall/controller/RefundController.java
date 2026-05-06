package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.*;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.User;
import com.minimall.service.RefundService;
import com.minimall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Refund", description = "Refund management APIs")
public class RefundController {
    private final RefundService refundService;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    public RefundController(RefundService refundService, SecurityUtils securityUtils, UserService userService) {
        this.refundService = refundService;
        this.securityUtils = securityUtils;
        this.userService = userService;
    }

    @PostMapping("/orders/{orderId}/refund")
    @Operation(summary = "Create a refund request for an order")
    public ResponseEntity<RefundRequestDTO> createRefund(
            @PathVariable String orderId,
            @RequestBody CreateRefundRequest request) {
        if (!securityUtils.isCurrentUser(request.userId)) {
            throw new UnauthorizedException("You can only create refund requests for yourself");
        }
        return ResponseEntity.ok(refundService.create(orderId, request.userId, request.amount, request.reason));
    }

    @GetMapping("/refunds")
    @Operation(summary = "Get all refund requests (admin)")
    public ResponseEntity<List<RefundRequestDTO>> getAllRefunds() {
        String currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Admin access required");
        }
        return ResponseEntity.ok(refundService.findAll());
    }

    @GetMapping("/refunds/{id}")
    @Operation(summary = "Get refund request by ID")
    public ResponseEntity<RefundRequestDTO> getRefund(@PathVariable String id) {
        RefundRequestDTO refund = refundService.findById(id);
        if (!securityUtils.isCurrentUser(refund.userId())) {
            String currentUserId = securityUtils.getCurrentUserId();
            User currentUser = userService.findById(currentUserId);
            if (currentUser.getRole() != User.Role.ADMIN) {
                throw new UnauthorizedException("You can only access your own refund requests");
            }
        }
        return ResponseEntity.ok(refund);
    }

    @PatchMapping("/refunds/{id}/approve")
    @Operation(summary = "Approve a refund request (admin)")
    public ResponseEntity<RefundRequestDTO> approveRefund(
            @PathVariable String id,
            @RequestBody ApproveRefundRequest request) {
        String currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Admin access required");
        }
        return ResponseEntity.ok(refundService.approve(id, request.adminNote));
    }

    @PatchMapping("/refunds/{id}/reject")
    @Operation(summary = "Reject a refund request (admin)")
    public ResponseEntity<RefundRequestDTO> rejectRefund(
            @PathVariable String id,
            @RequestBody RejectRefundRequest request) {
        String currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Admin access required");
        }
        return ResponseEntity.ok(refundService.reject(id, request.rejectReason));
    }

    @GetMapping("/refunds/status/{status}")
    @Operation(summary = "Get refund requests by status (admin)")
    public ResponseEntity<List<RefundRequestDTO>> getRefundsByStatus(
            @PathVariable com.minimall.model.RefundRequest.Status status) {
        String currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Admin access required");
        }
        return ResponseEntity.ok(refundService.findByStatus(status));
    }
}
