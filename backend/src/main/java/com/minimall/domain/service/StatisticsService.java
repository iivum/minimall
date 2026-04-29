package com.minimall.domain.service;

import com.minimall.domain.mapper.OrderMapper;
import com.minimall.domain.mapper.StatisticsMapper;
import com.minimall.domain.mapper.UserMapper;
import com.minimall.dto.StatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public StatisticsDTO getOverviewMetrics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        StatisticsDTO dto = new StatisticsDTO();

        Map<String, Object> metrics = statisticsMapper.getSummaryMetrics(start, end);
        dto.setTotalOrders(((Number) metrics.get("total_orders")).longValue());
        dto.setTotalGMV((BigDecimal) metrics.get("total_gmv"));
        dto.setAverageOrderValue((BigDecimal) metrics.get("avg_order_value"));

        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDateTime prevStart = start.minusDays(periodDays);
        LocalDateTime prevEnd = start;

        Map<String, Object> prevMetrics = statisticsMapper.getSummaryMetrics(prevStart, prevEnd);
        long prevOrders = ((Number) prevMetrics.get("total_orders")).longValue();
        BigDecimal prevGMV = (BigDecimal) prevMetrics.get("total_gmv");

        dto.setOrdersChange(calculatePercentChange(prevOrders, dto.getTotalOrders()));
        dto.setGmvChange(calculatePercentChange(prevGMV, dto.getTotalGMV()));

        long newUsers = statisticsMapper.countNewUsers(start, end);
        long prevNewUsers = statisticsMapper.countNewUsers(prevStart, prevEnd);
        dto.setTotalUsers(newUsers);
        dto.setUsersChange(calculatePercentChange(prevNewUsers, newUsers));

        dto.setDailyMetrics(getDailyMetrics(startDate, endDate));

        long todayDAU = statisticsMapper.getTodayActiveUsers();
        dto.setConversionRate(BigDecimal.valueOf(todayDAU)
            .divide(BigDecimal.valueOf(dto.getTotalUsers() > 0 ? dto.getTotalUsers() : 1), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)));

        return dto;
    }

    public List<StatisticsDTO.DailyMetric> getDailyMetrics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Map<String, Object>> rawData = statisticsMapper.getDailyMetrics(start, end);
        List<StatisticsDTO.DailyMetric> metrics = new ArrayList<>();

        Map<LocalDate, StatisticsDTO.DailyMetric> metricMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rawData) {
            StatisticsDTO.DailyMetric metric = new StatisticsDTO.DailyMetric();
            metric.setDate(((java.sql.Date) row.get("date")).toLocalDate());
            metric.setOrders(((Number) row.get("orders")).longValue());
            metric.setGmv((BigDecimal) row.get("gmv"));
            metric.setUsers(((Number) row.get("users")).longValue());
            metricMap.put(metric.getDate(), metric);
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            metrics.add(metricMap.getOrDefault(current, createEmptyMetric(current)));
            current = current.plusDays(1);
        }

        return metrics;
    }

    public List<Map<String, Object>> getUserGrowthData(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        return statisticsMapper.getDailyNewUsers(start, end);
    }

    private StatisticsDTO.DailyMetric createEmptyMetric(LocalDate date) {
        StatisticsDTO.DailyMetric metric = new StatisticsDTO.DailyMetric();
        metric.setDate(date);
        metric.setOrders(0L);
        metric.setGmv(BigDecimal.ZERO);
        metric.setUsers(0L);
        return metric;
    }

    private <T extends Number> long calculatePercentChange(T prev, T current) {
        if (prev == null || prev.doubleValue() == 0) {
            return current.doubleValue() > 0 ? 100 : 0;
        }
        return BigDecimal.valueOf(current.doubleValue())
            .subtract(BigDecimal.valueOf(prev.doubleValue()))
            .divide(BigDecimal.valueOf(prev.doubleValue()), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP)
            .longValue();
    }
}
