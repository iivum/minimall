package com.minimall.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StatisticsDTO {
    private long totalOrders;
    private BigDecimal totalGMV;
    private long totalUsers;
    private BigDecimal averageOrderValue;
    private long ordersChange;
    private BigDecimal gmvChange;
    private long usersChange;
    private BigDecimal conversionRate;
    private List<DailyMetric> dailyMetrics;

    @Data
    public static class DailyMetric {
        private LocalDate date;
        private long orders;
        private BigDecimal gmv;
        private long users;
    }
}
