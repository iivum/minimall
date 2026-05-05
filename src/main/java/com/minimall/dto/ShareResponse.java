package com.minimall.dto;

import java.time.Instant;

public record ShareResponse(
    String shareId,
    String shareUrl,
    String posterUrl,
    Instant expiresAt
) {}
