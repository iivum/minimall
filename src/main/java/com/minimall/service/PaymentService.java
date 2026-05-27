package com.minimall.service;

import com.minimall.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    public String initiatePayment(Order order, BigDecimal amount) {
        String paymentId = "PAY-" + UUID.randomUUID().toString();
        log.info("Initiated payment: {} for order: {} amount: {}", paymentId, order.getOrderNo(), amount);
        return paymentId;
    }

    public Map<String, Object> getPaymentStatus(String paymentId) {
        Map<String, Object> status = new HashMap<>();
        status.put("paymentId", paymentId);
        status.put("status", "PENDING");
        status.put("message", "Payment is processing");
        return status;
    }

    public void processCallback(String transactionId, String status) {
        log.info("Processing payment callback: transactionId={}, status={}", transactionId, status);
        if ("SUCCESS".equals(status)) {
            log.info("Payment successful: {}", transactionId);
        } else {
            log.warn("Payment failed: {}", transactionId);
        }
    }
}