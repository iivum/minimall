package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "AdminOrder", description = "Admin Order Management APIs")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders (admin, paginated)")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all orders (admin, non-paginated)")
    public ResponseEntity<List<Order>> getAllOrdersAll() {
        return ResponseEntity.ok(orderService.findAll());
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

    @GetMapping("/export")
    @Operation(summary = "Export orders to CSV (admin)")
    public void exportOrders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Order.Status status,
            @RequestParam(required = false) Order.PayStatus payStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response) throws IOException {

        Instant startInstant = startDate != null ? startDate.toInstant(ZoneOffset.UTC) : null;
        Instant endInstant = endDate != null ? endDate.toInstant(ZoneOffset.UTC) : null;

        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders.csv\"");

        try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write("﻿");
            writer.write("Order ID,Order No,User ID,User Nickname,Total Amount,Status,Pay Status,Pay Time,Created At\n");

            final int BATCH_SIZE = 1000;
            long offset = 0;

            while (true) {
                Pageable pageable = PageRequest.of((int) (offset / BATCH_SIZE), BATCH_SIZE);
                Page<Order> page = orderService.findByFilters(userId, status, payStatus, startInstant, endInstant, pageable);

                for (Order order : page.getContent()) {
                    writer.write(String.format("%s,%s,%s,%s,%.2f,%s,%s,%s,%s\n",
                            escape(order.getId()),
                            escape(order.getOrderNo()),
                            escape(order.getUser().getId()),
                            escape(order.getUser().getNickname()),
                            order.getTotalAmount(),
                            order.getStatus(),
                            order.getPayStatus(),
                            order.getPayTime() != null ? order.getPayTime().toString() : "",
                            order.getCreatedAt().toString()));
                }

                if (!page.hasNext()) {
                    break;
                }
                offset += BATCH_SIZE;
            }
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}