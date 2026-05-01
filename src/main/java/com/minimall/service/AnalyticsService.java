package com.minimall.service;

import com.minimall.model.AnalyticsEvent;
import com.minimall.repository.AnalyticsEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {
    private final AnalyticsEventRepository eventRepository;

    public AnalyticsService(AnalyticsEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public AnalyticsEvent track(String eventType, String userId, String targetType, String targetId, Map<String, Object> properties) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventType(eventType);
        event.setUserId(userId);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setProperties(properties != null ? mapToJson(properties) : null);
        return eventRepository.save(event);
    }

    public List<AnalyticsEvent> getUserEvents(String userId) {
        return eventRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AnalyticsEvent> getEventsByType(String eventType) {
        return eventRepository.findByEventTypeOrderByCreatedAtDesc(eventType);
    }

    public List<AnalyticsEvent> getDailyReport(LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return eventRepository.findByDateRange(start, end);
    }

    public Map<String, Object> getDailySummary(LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date.toString());
        summary.put("totalEvents", eventRepository.findByDateRange(start, end).size());

        List<Object[]> eventsByType = eventRepository.countEventsByType(start, end);
        Map<String, Long> eventsByTypeMap = new HashMap<>();
        for (Object[] row : eventsByType) {
            eventsByTypeMap.put((String) row[0], (Long) row[1]);
        }
        summary.put("eventsByType", eventsByTypeMap);

        List<Object[]> dailyCounts = eventRepository.dailyEventCounts(start, end);
        Map<String, Long> dailyCountsMap = new HashMap<>();
        for (Object[] row : dailyCounts) {
            dailyCountsMap.put(row[0].toString(), (Long) row[1]);
        }
        summary.put("dailyCounts", dailyCountsMap);

        return summary;
    }

    public Map<String, Object> getFunnelData(String startEventType, String endEventType, LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<AnalyticsEvent> startEvents = eventRepository.findByEventTypeOrderByCreatedAtDesc(startEventType);
        List<AnalyticsEvent> endEvents = eventRepository.findByEventTypeOrderByCreatedAtDesc(endEventType);

        long startCount = startEvents.stream()
            .filter(e -> !e.getCreatedAt().isBefore(start) && e.getCreatedAt().isBefore(end))
            .count();
        long endCount = endEvents.stream()
            .filter(e -> !e.getCreatedAt().isBefore(start) && e.getCreatedAt().isBefore(end))
            .count();

        Map<String, Object> funnel = new HashMap<>();
        funnel.put("startEvent", startEventType);
        funnel.put("endEvent", endEventType);
        funnel.put("startCount", startCount);
        funnel.put("endCount", endCount);
        funnel.put("conversionRate", startCount > 0 ? (double) endCount / startCount : 0.0);

        return funnel;
    }

    public List<AnalyticsEvent> getTopProducts(LocalDate date, int limit) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Object[]> results = eventRepository.countEventsByTargetType(start, end);
        return results.stream()
            .filter(row -> "PRODUCT".equals(row[0]))
            .limit(limit)
            .map(row -> eventRepository.findByEventTypeOrderByCreatedAtDesc("PRODUCT_VIEW").stream()
                .filter(e -> e.getTargetId() != null)
                .findFirst()
                .orElse(null))
            .toList();
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
