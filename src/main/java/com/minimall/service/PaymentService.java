package com.minimall.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    public Map<String, Object> initiatePayment(String orderId, BigDecimal amount) {
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("paymentId", "PAY-" + UUID.randomUUID().toString().substring(0, 8));
        paymentInfo.put("orderId", orderId);
        paymentInfo.put("amount", amount);
        paymentInfo.put("status", "PENDING");
        paymentInfo.put("timeStamp", System.currentTimeMillis() / 1000);
        return paymentInfo;
    }

    public Map<String, Object> getPaymentStatus(String paymentId) {
        Map<String, Object> status = new HashMap<>();
        status.put("paymentId", paymentId);
        status.put("status", "PENDING");
        return status;
    }

    public Map<String, Object> processCallback(String transactionId, String status) {
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", transactionId);
        result.put("status", status);
        result.put("received", true);
        return result;
    }
}