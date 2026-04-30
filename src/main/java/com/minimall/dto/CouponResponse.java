package com.minimall.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponResponse(
    String id,
    String code,
    String couponType,
    BigDecimal discountAmount,
    BigDecimal minOrderAmount,
    Instant validFrom,
    Instant validUntil,
    boolean isActive,
    Integer remainingQuantity
) {}
