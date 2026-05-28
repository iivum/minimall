package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import com.minimall.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Payment APIs")
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for an order")
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @RequestParam String orderId,
            @RequestParam BigDecimal amount) {
        Order order = orderService.findById(orderId);
        String paymentId = paymentService.initiatePayment(order, amount);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", paymentId);
        result.put("orderId", orderId);
        result.put("amount", amount);
        result.put("status", "PENDING");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{paymentId}")
    @Operation(summary = "Get payment status by payment ID")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        Map<String, Object> status = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle payment callback from WeChat Pay")
    public ResponseEntity<String> handleCallback(
            @RequestParam String transactionId,
            @RequestParam String status) {
        paymentService.processCallback(transactionId, status);
        return ResponseEntity.ok("SUCCESS");
    }
}