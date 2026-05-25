package com.minimall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
    @NotBlank String code,
    @NotNull @DecimalMin("0.01") BigDecimal discountAmount,
    @NotNull @DecimalMin("0.00") BigDecimal minOrderAmount,
    @NotNull Instant validFrom,
    @NotNull Instant validUntil,
    @NotNull @Min(1) Integer totalQuantity,
    @NotBlank String couponType
) {}
