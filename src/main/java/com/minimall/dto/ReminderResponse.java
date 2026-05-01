package com.minimall.dto;

import java.time.Instant;

public record ReminderResponse(
    String id,
    String reminderType,
    String status,
    String userId,
    String orderId,
    int sendCount,
    int maxSendCount,
    Instant nextSendTime,
    Instant sentAt
) {}