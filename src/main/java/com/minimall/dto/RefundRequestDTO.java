package com.minimall.dto;

import com.minimall.model.RefundRequest;
import java.math.BigDecimal;
import java.time.Instant;

public record RefundRequestDTO(
    String id,
    String orderId,
    String userId,
    String orderNo,
    BigDecimal amount,
    String status,
    String reason,
    String adminNote,
    String wechatRefundNo,
    Instant wechatRefundTime,
    String rejectReason,
    Instant createdAt,
    Instant updatedAt
) {
    public static RefundRequestDTO from(RefundRequest refund) {
        return new RefundRequestDTO(
            refund.getId(),
            refund.getOrderId(),
            refund.getUserId(),
            refund.getOrderNo(),
            refund.getAmount(),
            refund.getStatus().name(),
            refund.getReason(),
            refund.getAdminNote(),
            refund.getWechatRefundNo(),
            refund.getWechatRefundTime(),
            refund.getRejectReason(),
            refund.getCreatedAt(),
            refund.getUpdatedAt()
        );
    }
}
