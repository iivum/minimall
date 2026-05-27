package com.minimall.controller;

import com.minimall.dto.PaymentResponse;
import com.minimall.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Payment APIs")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for an order")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestParam String orderId,
            @RequestParam BigDecimal amount) {
        PaymentResponse response = paymentService.initiatePayment(orderId, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{paymentId}")
    @Operation(summary = "Get payment status by payment ID")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle payment callback")
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestParam String transactionId,
            @RequestParam String status) {
        paymentService.processCallback(transactionId, status);
        Map<String, String> result = new HashMap<>();
        result.put("code", "SUCCESS");
        result.put("message", "Callback processed successfully");
        return ResponseEntity.ok(result);
    }
}