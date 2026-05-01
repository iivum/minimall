package com.minimall.service;

import com.minimall.model.AnalyticsEvent;
import com.minimall.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsEventRepository eventRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(eventRepository);
    }

    @Test
    @DisplayName("track saves event with correct fields")
    void track_savesEventWithCorrectFields() {
        AnalyticsEvent savedEvent = new AnalyticsEvent();
        savedEvent.setId("test-id");
        savedEvent.setEventType("USER_REGISTER");
        savedEvent.setUserId("user-123");
        savedEvent.setTargetType("USER");
        savedEvent.setTargetId("user-123");

        when(eventRepository.save(any(AnalyticsEvent.class))).thenReturn(savedEvent);

        AnalyticsEvent result = analyticsService.track("USER_REGISTER", "user-123", "USER", "user-123", null);

        assertThat(result.getEventType()).isEqualTo("USER_REGISTER");
        assertThat(result.getUserId()).isEqualTo("user-123");
        verify(eventRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    @DisplayName("track with properties serializes to JSON")
    void track_withProperties_serializesToJson() {
        when(eventRepository.save(any(AnalyticsEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> properties = new HashMap<>();
        properties.put("key", "value");

        AnalyticsEvent result = analyticsService.track("TEST_EVENT", "user-1", "TYPE", "id-1", properties);

        assertThat(result.getProperties()).contains("key");
        assertThat(result.getProperties()).contains("value");
    }

    @Test
    @DisplayName("getUserEvents returns events for user")
    void getUserEvents_returnsEventsForUser() {
        AnalyticsEvent event1 = new AnalyticsEvent();
        event1.setId("e1");
        event1.setUserId("user-1");
        AnalyticsEvent event2 = new AnalyticsEvent();
        event2.setId("e2");
        event2.setUserId("user-1");

        when(eventRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
            .thenReturn(List.of(event1, event2));

        List<AnalyticsEvent> result = analyticsService.getUserEvents("user-1");

        assertThat(result).hasSize(2);
        verify(eventRepository).findByUserIdOrderByCreatedAtDesc("user-1");
    }

    @Test
    @DisplayName("getEventsByType returns events of type")
    void getEventsByType_returnsEventsOfType() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setId("e1");
        event.setEventType("PRODUCT_VIEW");

        when(eventRepository.findByEventTypeOrderByCreatedAtDesc("PRODUCT_VIEW"))
            .thenReturn(List.of(event));

        List<AnalyticsEvent> result = analyticsService.getEventsByType("PRODUCT_VIEW");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo("PRODUCT_VIEW");
    }

    @Test
    @DisplayName("getDailySummary returns correct summary structure")
    void getDailySummary_returnsCorrectSummaryStructure() {
        LocalDate date = LocalDate.of(2026, 5, 1);
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        when(eventRepository.findByDateRange(start, end)).thenReturn(List.of());
        when(eventRepository.countEventsByType(start, end)).thenReturn(List.of());
        when(eventRepository.dailyEventCounts(start, end)).thenReturn(List.of());

        Map<String, Object> summary = analyticsService.getDailySummary(date);

        assertThat(summary).containsKey("date");
        assertThat(summary).containsKey("totalEvents");
        assertThat(summary).containsKey("eventsByType");
        assertThat(summary).containsKey("dailyCounts");
    }
}
