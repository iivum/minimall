package com.minimall.controller;

import com.minimall.model.AnalyticsEvent;
import com.minimall.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "User behavior analytics APIs")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/track")
    @Operation(summary = "Track a user event")
    public ResponseEntity<AnalyticsEvent> trackEvent(@RequestBody TrackRequest request) {
        AnalyticsEvent event = analyticsService.track(
            request.eventType(),
            request.userId(),
            request.targetType(),
            request.targetId(),
            request.properties()
        );
        return ResponseEntity.ok(event);
    }

    @GetMapping("/users/{userId}/events")
    @Operation(summary = "Get all events for a user")
    public ResponseEntity<List<AnalyticsEvent>> getUserEvents(@PathVariable String userId) {
        return ResponseEntity.ok(analyticsService.getUserEvents(userId));
    }

    @GetMapping("/events/{eventType}")
    @Operation(summary = "Get events by type")
    public ResponseEntity<List<AnalyticsEvent>> getEventsByType(@PathVariable String eventType) {
        return ResponseEntity.ok(analyticsService.getEventsByType(eventType));
    }

    @GetMapping("/reports/daily")
    @Operation(summary = "Get daily event report")
    public ResponseEntity<Map<String, Object>> getDailyReport(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate date) {
        return ResponseEntity.ok(analyticsService.getDailySummary(date));
    }

    @GetMapping("/funnel")
    @Operation(summary = "Get funnel conversion data")
    public ResponseEntity<Map<String, Object>> getFunnel(
            @RequestParam String startEvent,
            @RequestParam String endEvent,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getFunnelData(startEvent, endEvent, startDate, endDate));
    }

    public record TrackRequest(
        String eventType,
        String userId,
        String targetType,
        String targetId,
        Map<String, Object> properties
    ) {}
}
