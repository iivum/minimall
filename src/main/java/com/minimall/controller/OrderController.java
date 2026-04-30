package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<Order> getOrderByNo(@PathVariable String orderNo) {
        return ResponseEntity.ok(orderService.findByOrderNo(orderNo));
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request.userId, request.items));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<Order> updateStatus(@PathVariable String id, @RequestParam Order.Status status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Mark order as paid")
    public ResponseEntity<Order> payOrder(@PathVariable String id, @RequestParam String tradeNo) {
        return ResponseEntity.ok(orderService.pay(id, tradeNo));
    }

    public static class CreateOrderRequest {
        public String userId;
        public List<OrderItem> items;
    }
}
