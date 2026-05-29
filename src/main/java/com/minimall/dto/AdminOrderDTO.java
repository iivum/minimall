package com.minimall.dto;

import com.minimall.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AdminOrderDTO {
    private String id;
    private String orderNo;
    private String userId;
    private String userNickname;
    private BigDecimal totalAmount;
    private String status;
    private String payStatus;
    private Instant payTime;
    private String tradeNo;
    private List<OrderItemDTO> items;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    public Instant getPayTime() { return payTime; }
    public void setPayTime(Instant payTime) { this.payTime = payTime; }
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static AdminOrderDTO from(Order order) {
        AdminOrderDTO dto = new AdminOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setUserNickname(order.getUser() != null ? order.getUser().getNickname() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setPayStatus(order.getPayStatus() != null ? order.getPayStatus().name() : null);
        dto.setPayTime(order.getPayTime());
        dto.setTradeNo(order.getTradeNo());
        dto.setCreatedAt(order.getCreatedAt());
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream().map(item -> {
                OrderItemDTO itemDTO = new OrderItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
                itemDTO.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                return itemDTO;
            }).toList());
        }
        return dto;
    }

    public static class OrderItemDTO {
        private String id;
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}