package com.minimall.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DeductPointsRequest(
    @NotNull int points,
    String orderNo,
    String description
) {}