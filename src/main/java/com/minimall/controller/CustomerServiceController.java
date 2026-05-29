package com.minimall.controller;

import com.minimall.dto.CustomerServiceMessageDTO;
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

    public CustomerServiceController(CustomerServiceService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/receive")
    @Operation(summary = "Receive customer message from WeChat")
    public ResponseEntity<CustomerServiceMessageDTO> receiveMessage(
            @RequestParam String openid,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") CustomerServiceMessage.MessageType type) {
        CustomerServiceMessage message = customerService.receiveMessage(openid, content, type);
        return ResponseEntity.ok(CustomerServiceMessageDTO.from(message));
    }

    @GetMapping("/messages/{openid}")
    @Operation(summary = "Get user's message history")
    public ResponseEntity<List<CustomerServiceMessageDTO>> getUserMessages(@PathVariable String openid) {
        List<CustomerServiceMessageDTO> dtos = customerService.getUserMessages(openid).stream()
                .map(CustomerServiceMessageDTO::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending messages (for handler dashboard)")
    public ResponseEntity<List<CustomerServiceMessageDTO>> getPendingMessages() {
        List<CustomerServiceMessageDTO> dtos = customerService.getPendingMessages().stream()
                .map(CustomerServiceMessageDTO::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<CustomerServiceMessageDTO> markAsRead(@PathVariable String messageId) {
        return ResponseEntity.ok(CustomerServiceMessageDTO.from(customerService.markAsRead(messageId)));
    }

    @PostMapping("/{messageId}/complete")
    @Operation(summary = "Mark message as completed")
    public ResponseEntity<CustomerServiceMessageDTO> completeMessage(@PathVariable String messageId) {
        return ResponseEntity.ok(CustomerServiceMessageDTO.from(customerService.completeMessage(messageId)));
    }

    @PostMapping("/{messageId}/transfer")
    @Operation(summary = "Transfer message to human handler")
    public ResponseEntity<CustomerServiceMessageDTO> transferToHuman(
            @PathVariable String messageId,
            @RequestParam String handlerId) {
        return ResponseEntity.ok(CustomerServiceMessageDTO.from(customerService.transferToHuman(messageId, handlerId)));
    }

    @GetMapping("/stats/pending-count")
    @Operation(summary = "Get pending message count")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(customerService.getPendingCount());
    }
}