package com.minimall.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    String paymentId,
    String orderId,
    BigDecimal amount,
    String status,
    String transactionId,
    Instant createdAt,
    Instant paidAt
) {
    public static PaymentResponse pending(String orderId, BigDecimal amount) {
        return new PaymentResponse(
            null,
            orderId,
            amount,
            "PENDING",
            null,
            Instant.now(),
            null
        );
    }

    public static PaymentResponse success(String paymentId, String orderId, BigDecimal amount, String transactionId) {
        return new PaymentResponse(
            paymentId,
            orderId,
            amount,
            "SUCCESS",
            transactionId,
            Instant.now(),
            Instant.now()
        );
    }
}