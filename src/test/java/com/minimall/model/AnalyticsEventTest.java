package com.minimall.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsEventTest {

    @Test
    @DisplayName("onCreate sets createdAt when null")
    void onCreate_setsCreatedAt_whenNull() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventType("TEST");
        event.setUserId("user-1");

        event.onCreate();

        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("onCreate preserves existing createdAt")
    void onCreate_preservesExistingCreatedAt() {
        AnalyticsEvent event = new AnalyticsEvent();
        Instant originalTime = Instant.parse("2026-01-01T00:00:00Z");
        event.setCreatedAt(originalTime);
        event.setEventType("TEST");
        event.setUserId("user-1");

        event.onCreate();

        assertThat(event.getCreatedAt()).isEqualTo(originalTime);
    }

    @Test
    @DisplayName("getters and setters work correctly")
    void gettersAndSetters_workCorrectly() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setId("test-id");
        event.setEventType("USER_REGISTER");
        event.setUserId("user-123");
        event.setTargetType("USER");
        event.setTargetId("user-123");
        event.setProperties("{\"key\":\"value\"}");

        assertThat(event.getId()).isEqualTo("test-id");
        assertThat(event.getEventType()).isEqualTo("USER_REGISTER");
        assertThat(event.getUserId()).isEqualTo("user-123");
        assertThat(event.getTargetType()).isEqualTo("USER");
        assertThat(event.getTargetId()).isEqualTo("user-123");
        assertThat(event.getProperties()).isEqualTo("{\"key\":\"value\"}");
    }
}
