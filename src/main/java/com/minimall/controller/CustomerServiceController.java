package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.CustomerServiceMessage;
import com.minimall.service.CustomerServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-service")
@Tag(name = "Customer Service", description = "WeChat customer service message APIs")
public class CustomerServiceController {
    private final CustomerServiceService customerService;
    private final SecurityUtils securityUtils;

    public CustomerServiceController(CustomerServiceService customerService, SecurityUtils securityUtils) {
        this.customerService = customerService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/receive")
    @Operation(summary = "Receive customer message from WeChat")
    public ResponseEntity<CustomerServiceMessage> receiveMessage(
            @RequestParam String openid,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") CustomerServiceMessage.MessageType type) {
        // Verify the user is the one sending the message
        String currentUserOpenid = securityUtils.getCurrentUserOpenid();
        if (currentUserOpenid == null || !currentUserOpenid.equals(openid)) {
            throw new UnauthorizedException("You can only send messages for yourself");
        }
        // Validate input
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Message content too long (max 1000 characters)");
        }
        CustomerServiceMessage message = customerService.receiveMessage(openid, content.trim(), type);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/messages")
    @Operation(summary = "Get current user's message history")
    public ResponseEntity<List<CustomerServiceMessage>> getUserMessages() {
        String currentUserOpenid = securityUtils.getCurrentUserOpenid();
        if (currentUserOpenid == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(customerService.getUserMessages(currentUserOpenid));
    }

    @GetMapping("/handler/pending")
    @Operation(summary = "Get all pending messages (for handler dashboard)")
    public ResponseEntity<List<CustomerServiceMessage>> getPendingMessages() {
        // Handler endpoints require authentication - protected by Spring Security
        return ResponseEntity.ok(customerService.getPendingMessages());
    }

    @PostMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<CustomerServiceMessage> markAsRead(@PathVariable String messageId) {
        String currentUserOpenid = securityUtils.getCurrentUserOpenid();
        if (currentUserOpenid == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        CustomerServiceMessage message = customerService.findById(messageId);
        if (!message.getOpenid().equals(currentUserOpenid)) {
            throw new UnauthorizedException("You can only mark your own messages as read");
        }
        return ResponseEntity.ok(customerService.markAsRead(messageId));
    }

    @PostMapping("/{messageId}/complete")
    @Operation(summary = "Mark message as completed")
    public ResponseEntity<CustomerServiceMessage> completeMessage(@PathVariable String messageId) {
        String currentUserOpenid = securityUtils.getCurrentUserOpenid();
        if (currentUserOpenid == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(customerService.completeMessage(messageId));
    }

    @PostMapping("/{messageId}/transfer")
    @Operation(summary = "Transfer message to human handler")
    public ResponseEntity<CustomerServiceMessage> transferToHuman(
            @PathVariable String messageId,
            @RequestParam String handlerId) {
        String currentUserOpenid = securityUtils.getCurrentUserOpenid();
        if (currentUserOpenid == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        if (handlerId == null || handlerId.isBlank()) {
            throw new IllegalArgumentException("Handler ID is required");
        }
        return ResponseEntity.ok(customerService.transferToHuman(messageId, handlerId));
    }

    @GetMapping("/stats/pending-count")
    @Operation(summary = "Get pending message count")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(customerService.getPendingCount());
    }
}
