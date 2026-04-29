package com.minimall.controller;

import com.minimall.domain.service.StatisticsService;
import com.minimall.dto.StatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public ResponseEntity<StatisticsDTO> getOverviewMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statisticsService.getOverviewMetrics(startDate, endDate));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<StatisticsDTO.DailyMetric>> getDailyMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statisticsService.getDailyMetrics(startDate, endDate));
    }

    @GetMapping("/users/growth")
    public ResponseEntity<List<Map<String, Object>>> getUserGrowth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statisticsService.getUserGrowthData(startDate, endDate));
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {

        List<StatisticsDTO.DailyMetric> metrics = statisticsService.getDailyMetrics(startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("format", format);
        response.put("data", metrics);
        response.put("generatedAt", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getQuickSummary() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        StatisticsDTO metrics = statisticsService.getOverviewMetrics(weekAgo, today);

        Map<String, Object> summary = new HashMap<>();
        List<StatisticsDTO.DailyMetric> todayMetrics = statisticsService.getDailyMetrics(today, today);
        if (!todayMetrics.isEmpty()) {
            summary.put("todayOrders", todayMetrics.get(0).getOrders());
            summary.put("todayGMV", todayMetrics.get(0).getGmv());
            summary.put("todayNewUsers", todayMetrics.get(0).getUsers());
        } else {
            summary.put("todayOrders", 0);
            summary.put("todayGMV", BigDecimal.ZERO);
            summary.put("todayNewUsers", 0);
        }
        summary.put("weekOrders", metrics.getTotalOrders());
        summary.put("weekGMV", metrics.getTotalGMV());
        summary.put("weekNewUsers", metrics.getTotalUsers());
        summary.put("avgOrderValue", metrics.getAverageOrderValue());

        return ResponseEntity.ok(summary);
    }
}
