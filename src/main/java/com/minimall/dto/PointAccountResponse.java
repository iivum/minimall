package com.minimall.dto;

import com.minimall.model.PointAccount;
import java.math.BigDecimal;

public record PointAccountResponse(
    String id,
    String userId,
    BigDecimal balance,
    BigDecimal totalEarned,
    BigDecimal totalSpent
) {
    public static PointAccountResponse from(PointAccount account) {
        return new PointAccountResponse(
            account.getId(),
            account.getUser().getId(),
            account.getBalance(),
            account.getTotalEarned(),
            account.getTotalSpent()
        );
    }
}