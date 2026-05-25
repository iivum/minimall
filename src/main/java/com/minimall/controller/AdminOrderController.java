package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "AdminOrder", description = "Admin Order Management APIs")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders (admin)")
    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (admin)")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "Get order by order number (admin)")
    public ResponseEntity<Order> getOrderByNo(@PathVariable String orderNo) {
        return ResponseEntity.ok(orderService.findByOrderNo(orderNo));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (admin)")
    public ResponseEntity<Order> updateStatus(@PathVariable String id, @RequestParam Order.Status status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}