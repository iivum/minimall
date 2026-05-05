package com.minimall.dto;

import com.minimall.model.PointTransaction;
import java.math.BigDecimal;
import java.time.Instant;

public record PointTransactionResponse(
    String id,
    BigDecimal amount,
    String type,
    Integer points,
    String orderNo,
    String description,
    Instant createdAt
) {
    public static PointTransactionResponse from(PointTransaction tx) {
        return new PointTransactionResponse(
            tx.getId(),
            tx.getAmount(),
            tx.getType().name(),
            tx.getPoints(),
            tx.getOrderNo(),
            tx.getDescription(),
            tx.getCreatedAt()
        );
    }
}