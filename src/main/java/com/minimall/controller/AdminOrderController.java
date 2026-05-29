package com.minimall.controller;

import com.minimall.dto.AdminOrderDTO;
import com.minimall.model.Order;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "AdminOrder", description = "Admin Order Management APIs")
public class AdminOrderController {
    private final OrderService orderService;
    private static final int EXPORT_BATCH_SIZE = 1000;
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders (admin, paginated)")
    public ResponseEntity<Page<AdminOrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderService.findAll(pageable);
        Page<AdminOrderDTO> dtoPage = orders.map(AdminOrderDTO::from);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all orders (admin, non-paginated)")
    public ResponseEntity<List<AdminOrderDTO>> getAllOrdersAll() {
        List<AdminOrderDTO> dtos = orderService.findAll().stream().map(AdminOrderDTO::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (admin)")
    public ResponseEntity<AdminOrderDTO> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(AdminOrderDTO.from(orderService.findById(id)));
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "Get order by order number (admin)")
    public ResponseEntity<AdminOrderDTO> getOrderByNo(@PathVariable String orderNo) {
        return ResponseEntity.ok(AdminOrderDTO.from(orderService.findByOrderNo(orderNo)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (admin)")
    public ResponseEntity<AdminOrderDTO> updateStatus(@PathVariable String id, @RequestParam Order.Status status) {
        return ResponseEntity.ok(AdminOrderDTO.from(orderService.updateStatus(id, status)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export orders to CSV (admin, streaming)")
    public void exportOrders(
            @RequestParam(required = false) Order.Status status,
            @RequestParam(required = false) Order.PayStatus payStatus,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders.csv\"");

        Pageable pageable = PageRequest.of(0, EXPORT_BATCH_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        int page = 0;
        boolean hasMore = true;

        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write("﻿");
            writer.write("订单号,用户ID,用户昵称,总金额,订单状态,支付状态,支付时间,创建时间\n");

            while (hasMore) {
                Page<Order> batch = orderService.findByFilters(status, payStatus, userId, startDate, endDate, pageable);
                for (Order order : batch.getContent()) {
                    writer.write(String.format("%s,%s,%s,%.2f,%s,%s,%s,%s\n",
                        order.getOrderNo(),
                        order.getUser().getId(),
                        escapeCsv(order.getUser().getNickname()),
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getPayStatus(),
                        order.getPayTime() != null ? EXPORT_DATE_FORMAT.format(order.getPayTime().atZone(java.time.ZoneId.systemDefault())) : "",
                        EXPORT_DATE_FORMAT.format(order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()))
                    ));
                }
                hasMore = batch.hasNext();
                pageable = PageRequest.of(++page, EXPORT_BATCH_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}