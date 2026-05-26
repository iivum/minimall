package com.minimall.dto;

import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.Product;
import java.util.List;

public final class DtoMapper {

    private DtoMapper() {}

    public static ProductDTO from(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.getActive());
        return dto;
    }

    public static List<ProductDTO> fromProducts(List<Product> products) {
        return products.stream().map(DtoMapper::from).toList();
    }

    public static OrderDTO from(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUser().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setPayStatus(order.getPayStatus().name());
        dto.setPayTime(order.getPayTime());
        dto.setTradeNo(order.getTradeNo());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItems(order.getItems().stream().map(DtoMapper::fromItem).toList());
        return dto;
    }

    public static OrderDTO.OrderItemDTO fromItem(OrderItem item) {
        OrderDTO.OrderItemDTO dto = new OrderDTO.OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }

    public static List<OrderDTO> fromOrders(List<Order> orders) {
        return orders.stream().map(DtoMapper::from).toList();
    }
}