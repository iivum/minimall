package com.minimall.dto;

import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDTO(
    String id,
    String orderNo,
    String userId,
    BigDecimal totalAmount,
    String status,
    String payStatus,
    Instant payTime,
    String tradeNo,
    List<OrderItemResponseDTO> items,
    Instant createdAt
) {
    public record OrderItemResponseDTO(
        String id,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal price
    ) {}

    public static OrderResponseDTO from(Order order) {
        return new OrderResponseDTO(
            order.getId(),
            order.getOrderNo(),
            order.getUser().getId(),
            order.getTotalAmount(),
            order.getStatus().name(),
            order.getPayStatus().name(),
            order.getPayTime(),
            order.getTradeNo(),
            order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
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