package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID (paginated)")
    public ResponseEntity<Page<Order>> getUserOrders(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!securityUtils.isCurrentUser(userId)) {
            throw new UnauthorizedException("You can only access your own orders");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(orderService.findByUserId(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        Order order = orderService.findById(id);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only access your own orders");
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<Order> getOrderByNo(@PathVariable String orderNo) {
        Order order = orderService.findByOrderNo(orderNo);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only access your own orders");
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        if (!securityUtils.isCurrentUser(request.userId)) {
            throw new UnauthorizedException("You can only create orders for yourself");
        }
        return ResponseEntity.ok(orderService.create(request.userId, request.items));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<Order> updateStatus(@PathVariable String id, @RequestParam Order.Status status) {
        Order order = orderService.findById(id);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only update your own orders");
        }
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Mark order as paid")
    public ResponseEntity<Order> payOrder(@PathVariable String id, @RequestParam String tradeNo) {
        Order order = orderService.findById(id);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only pay your own orders");
        }
        return ResponseEntity.ok(orderService.pay(id, tradeNo));
    }

    public static class CreateOrderRequest {
        public String userId;
        public List<OrderItem> items;
    }
}
