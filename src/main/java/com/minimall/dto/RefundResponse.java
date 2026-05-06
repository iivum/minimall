package com.minimall.dto;

import com.minimall.model.RefundRequest;
import java.math.BigDecimal;
import java.time.Instant;

public record RefundResponse(
    String id,
    String orderId,
    String orderNo,
    BigDecimal amount,
    String reason,
    String status,
    String adminComment,
    String adminId,
    Instant createdAt,
    Instant processedAt
) {
    public static RefundResponse from(RefundRequest refund) {
        return new RefundResponse(
            refund.getId(),
            refund.getOrder().getId(),
            refund.getOrder().getOrderNo(),
            refund.getAmount(),
            refund.getReason(),
            refund.getStatus().name(),
            refund.getAdminComment(),
            refund.getAdmin() != null ? refund.getAdmin().getId() : null,
            refund.getCreatedAt(),
            refund.getProcessedAt()
        );
    }
}