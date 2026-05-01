package com.minimall.dto;

import java.time.Instant;

public record ReminderRequest(
    String userId,
    String orderId,
    String reminderType,
    Instant scheduledTime
) {}