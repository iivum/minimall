package com.minimall.domain.service;

import com.minimall.domain.mapper.OrderMapper;
import com.minimall.domain.mapper.StatisticsMapper;
import com.minimall.domain.mapper.UserMapper;
import com.minimall.dto.StatisticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsMapper statisticsMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 7);
    }

    @Test
    @DisplayName("getOverviewMetrics returns correct metrics for date range")
    void getOverviewMetrics_returnsCorrectMetrics() {
        Map<String, Object> currentMetrics = new HashMap<>();
        currentMetrics.put("total_orders", 100L);
        currentMetrics.put("total_gmv", new BigDecimal("5000.00"));
        currentMetrics.put("avg_order_value", new BigDecimal("50.00"));

        Map<String, Object> prevMetrics = new HashMap<>();
        prevMetrics.put("total_orders", 80L);
        prevMetrics.put("total_gmv", new BigDecimal("4000.00"));
        prevMetrics.put("avg_order_value", new BigDecimal("50.00"));

        when(statisticsMapper.getSummaryMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(currentMetrics)
            .thenReturn(prevMetrics);
        when(statisticsMapper.countNewUsers(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(25L)
            .thenReturn(20L);
        when(statisticsMapper.getTodayActiveUsers()).thenReturn(10L);
        when(statisticsMapper.getDailyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        StatisticsDTO result = statisticsService.getOverviewMetrics(startDate, endDate);

        assertThat(result.getTotalOrders()).isEqualTo(100L);
        assertThat(result.getTotalGMV()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getTotalUsers()).isEqualTo(25L);
        assertThat(result.getAverageOrderValue()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getOrdersChange()).isEqualTo(25L);
        assertThat(result.getGmvChange()).isEqualTo(25L);
        assertThat(result.getUsersChange()).isEqualTo(25L);
    }

    @Test
    @DisplayName("getOverviewMetrics handles zero previous values correctly")
    void getOverviewMetrics_handlesZeroPreviousValues() {
        Map<String, Object> currentMetrics = new HashMap<>();
        currentMetrics.put("total_orders", 50L);
        currentMetrics.put("total_gmv", new BigDecimal("2500.00"));
        currentMetrics.put("avg_order_value", new BigDecimal("50.00"));

        Map<String, Object> prevMetrics = new HashMap<>();
        prevMetrics.put("total_orders", 0L);
        prevMetrics.put("total_gmv", BigDecimal.ZERO);
        prevMetrics.put("avg_order_value", BigDecimal.ZERO);

        when(statisticsMapper.getSummaryMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(currentMetrics)
            .thenReturn(prevMetrics);
        when(statisticsMapper.countNewUsers(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(10L)
            .thenReturn(0L);
        when(statisticsMapper.getTodayActiveUsers()).thenReturn(5L);
        when(statisticsMapper.getDailyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        StatisticsDTO result = statisticsService.getOverviewMetrics(startDate, endDate);

        assertThat(result.getTotalOrders()).isEqualTo(50L);
        assertThat(result.getOrdersChange()).isEqualTo(100L);
        assertThat(result.getGmvChange()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getDailyMetrics returns metrics for each day in range")
    void getDailyMetrics_returnsMetricsForEachDay() {
        List<Map<String, Object>> rawData = new ArrayList<>();

        Map<String, Object> day1 = new HashMap<>();
        day1.put("date", java.sql.Date.valueOf(LocalDate.of(2024, 1, 1)));
        day1.put("orders", 10L);
        day1.put("gmv", new BigDecimal("500.00"));
        day1.put("users", 5L);
        rawData.add(day1);

        Map<String, Object> day2 = new HashMap<>();
        day2.put("date", java.sql.Date.valueOf(LocalDate.of(2024, 1, 2)));
        day2.put("orders", 15L);
        day2.put("gmv", new BigDecimal("750.00"));
        day2.put("users", 8L);
        rawData.add(day2);

        when(statisticsMapper.getDailyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(rawData);

        List<StatisticsDTO.DailyMetric> result = statisticsService.getDailyMetrics(startDate, endDate);

        assertThat(result).hasSize(7);
        assertThat(result.get(0).getOrders()).isEqualTo(10L);
        assertThat(result.get(0).getGmv()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.get(1).getOrders()).isEqualTo(15L);
        assertThat(result.get(1).getGmv()).isEqualByComparingTo(new BigDecimal("750.00"));
    }

    @Test
    @DisplayName("getDailyMetrics fills missing days with empty metrics")
    void getDailyMetrics_fillsMissingDaysWithEmptyMetrics() {
        List<Map<String, Object>> rawData = new ArrayList<>();

        Map<String, Object> day1 = new HashMap<>();
        day1.put("date", java.sql.Date.valueOf(LocalDate.of(2024, 1, 1)));
        day1.put("orders", 10L);
        day1.put("gmv", new BigDecimal("500.00"));
        day1.put("users", 5L);
        rawData.add(day1);

        when(statisticsMapper.getDailyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(rawData);

        List<StatisticsDTO.DailyMetric> result = statisticsService.getDailyMetrics(startDate, endDate);

        assertThat(result).hasSize(7);
        assertThat(result.get(0).getOrders()).isEqualTo(10L);
        assertThat(result.get(1).getOrders()).isEqualTo(0L);
        assertThat(result.get(1).getGmv()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(1).getUsers()).isEqualTo(0L);
    }

    @Test
    @DisplayName("getUserGrowthData returns daily new users")
    void getUserGrowthData_returnsDailyNewUsers() {
        List<Map<String, Object>> expected = new ArrayList<>();
        Map<String, Object> day1 = new HashMap<>();
        day1.put("date", java.sql.Date.valueOf(LocalDate.of(2024, 1, 1)));
        day1.put("users", 5L);
        expected.add(day1);

        when(statisticsMapper.getDailyNewUsers(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expected);

        List<Map<String, Object>> result = statisticsService.getUserGrowthData(startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("users")).isEqualTo(5L);
    }

    @Test
    @DisplayName("calculatePercentChange returns 100 when previous is zero and current is positive")
    void calculatePercentChange_returns100WhenPreviousIsZero() {
        List<Map<String, Object>> currentMetrics = new HashMap<>();
        currentMetrics.put("total_orders", 50L);
        currentMetrics.put("total_gmv", new BigDecimal("2500.00"));
        currentMetrics.put("avg_order_value", new BigDecimal("50.00"));

        Map<String, Object> prevMetrics = new HashMap<>();
        prevMetrics.put("total_orders", 0L);
        prevMetrics.put("total_gmv", BigDecimal.ZERO);
        prevMetrics.put("avg_order_value", BigDecimal.ZERO);

        when(statisticsMapper.getSummaryMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(currentMetrics)
            .thenReturn(prevMetrics);
        when(statisticsMapper.countNewUsers(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(10L)
            .thenReturn(0L);
        when(statisticsMapper.getTodayActiveUsers()).thenReturn(5L);
        when(statisticsMapper.getDailyMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        StatisticsDTO result = statisticsService.getOverviewMetrics(startDate, endDate);

        assertThat(result.getOrdersChange()).isEqualTo(100L);
    }
}
