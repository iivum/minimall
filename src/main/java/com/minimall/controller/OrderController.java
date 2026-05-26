package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.DtoMapper;
import com.minimall.dto.OrderDTO;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @Operation(summary = "Get orders by user ID")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable String userId) {
        if (!securityUtils.isCurrentUser(userId)) {
            throw new UnauthorizedException("You can only access your own orders");
        }
        return ResponseEntity.ok(DtoMapper.fromOrders(orderService.findByUserId(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String id) {
        Order order = orderService.findById(id);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only access your own orders");
        }
        return ResponseEntity.ok(DtoMapper.from(order));
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<OrderDTO> getOrderByNo(@PathVariable String orderNo) {
        Order order = orderService.findByOrderNo(orderNo);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only access your own orders");
        }
        return ResponseEntity.ok(DtoMapper.from(order));
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        if (!securityUtils.isCurrentUser(request.userId)) {
            throw new UnauthorizedException("You can only create orders for yourself");
        }
        return ResponseEntity.ok(DtoMapper.from(orderService.create(request.userId, request.items)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderDTO> updateStatus(@PathVariable String id, @RequestParam Order.Status status) {
        Order order = orderService.findById(id);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only update your own orders");
        }
        return ResponseEntity.ok(DtoMapper.from(orderService.updateStatus(id, status)));
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Mark order as paid")
    public ResponseEntity<OrderDTO> payOrder(@PathVariable String id, @RequestParam String tradeNo) {
        Order order = orderService.findById(id);
        if (!securityUtils.isCurrentUser(order.getUser().getId())) {
            throw new UnauthorizedException("You can only pay your own orders");
        }
        return ResponseEntity.ok(DtoMapper.from(orderService.pay(id, tradeNo)));
    }

    public static class CreateOrderRequest {
        @NotNull
        public String userId;
        @NotEmpty
        public List<OrderItem> items;
    }
}
