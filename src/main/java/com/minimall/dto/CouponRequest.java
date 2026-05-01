package com.minimall.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
    String code,
    BigDecimal discountAmount,
    BigDecimal minOrderAmount,
    Instant validFrom,
    Instant validUntil,
    Integer totalQuantity,
    String couponType
) {}
