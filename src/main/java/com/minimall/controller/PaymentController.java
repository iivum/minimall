package com.minimall.controller;

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
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @RequestParam String orderId,
            @RequestParam BigDecimal amount) {
        Map<String, Object> paymentInfo = paymentService.initiatePayment(orderId, amount);
        return ResponseEntity.ok(paymentInfo);
    }

    @GetMapping("/status/{paymentId}")
    @Operation(summary = "Get payment status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        Map<String, Object> status = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/callback")
    @Operation(summary = "Payment callback notification")
    public ResponseEntity<Map<String, Object>> callbackPayment(
            @RequestParam String transactionId,
            @RequestParam String status) {
        Map<String, Object> result = paymentService.processCallback(transactionId, status);
        return ResponseEntity.ok(result);
    }
}