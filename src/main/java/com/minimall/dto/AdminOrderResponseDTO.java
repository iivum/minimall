package com.minimall.dto;

import com.minimall.model.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminOrderResponseDTO(
    String id,
    String orderNo,
    String userId,
    String userNickname,
    BigDecimal totalAmount,
    String status,
    String payStatus,
    Instant payTime,
    String tradeNo,
    List<AdminOrderItemDTO> items,
    Instant createdAt
) {
    public record AdminOrderItemDTO(
        String id,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal price
    ) {}

    public static AdminOrderResponseDTO from(Order order) {
        return new AdminOrderResponseDTO(
            order.getId(),
            order.getOrderNo(),
            order.getUser().getId(),
            order.getUser().getNickname(),
            order.getTotalAmount(),
            order.getStatus().name(),
            order.getPayStatus().name(),
            order.getPayTime(),
            order.getTradeNo(),
            order.getItems().isEmpty() ? null : order.getItems().stream()
                .map(item -> new AdminOrderItemDTO(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getPrice()
                ))
                .toList(),
            order.getCreatedAt()
        );
    }
}