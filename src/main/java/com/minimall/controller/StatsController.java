package com.minimall.controller;

import com.minimall.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
@Tag(name = "Stats", description = "Admin Statistics APIs")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<StatsService.DashboardStats> dashboard() {
        return ResponseEntity.ok(statsService.getDashboardStats());
    }

    @GetMapping("/orders-trend")
    @Operation(summary = "Get orders trend for last N days")
    public ResponseEntity<?> ordersTrend(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(statsService.getOrdersTrend(days));
    }
}