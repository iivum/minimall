package com.minimall.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReminderDtoTest {

    @Test
    void reminderResponse_constructor_works() {
        java.time.Instant now = java.time.Instant.now();
        ReminderResponse response = new ReminderResponse(
            "rem-123", "PURCHASE_COMPLETE", "PENDING",
            "user-123", "order-456", 0, 3, now, null
        );
        assertEquals("rem-123", response.id());
        assertEquals("PURCHASE_COMPLETE", response.reminderType());
    }

    @Test
    void reminderRequest_constructor_works() {
        java.time.Instant scheduledTime = java.time.Instant.now().plusSeconds(3600);
        ReminderRequest request = new ReminderRequest(
            "user-123", "order-456", "PURCHASE_COMPLETE", scheduledTime
        );
        assertEquals("user-123", request.userId());
        assertEquals("order-456", request.orderId());
    }
}