package com.minimall.dto;

import java.math.BigDecimal;

public record DeductPointsRequest(
    int points,
    String orderNo,
    String description
) {}