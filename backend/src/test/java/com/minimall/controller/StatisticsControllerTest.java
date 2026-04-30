package com.minimall.controller;

import com.minimall.domain.service.StatisticsService;
import com.minimall.dto.StatisticsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("GET /api/statistics/overview returns metrics for date range")
    void getOverviewMetrics_returnsMetrics() throws Exception {
        StatisticsDTO dto = new StatisticsDTO();
        dto.setTotalOrders(100L);
        dto.setTotalGMV(new BigDecimal("5000.00"));
        dto.setTotalUsers(25L);
        dto.setAverageOrderValue(new BigDecimal("50.00"));
        dto.setOrdersChange(25L);
        dto.setGmvChange(new BigDecimal("25.00"));
        dto.setUsersChange(20L);
        dto.setConversionRate(new BigDecimal("10.00"));
        dto.setDailyMetrics(Collections.emptyList());

        when(statisticsService.getOverviewMetrics(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(dto);

        mockMvc.perform(get("/api/statistics/overview")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalOrders", is(100)))
            .andExpect(jsonPath("$.totalGMV", is(5000.00)))
            .andExpect(jsonPath("$.totalUsers", is(25)))
            .andExpect(jsonPath("$.ordersChange", is(25)));
    }

    @Test
    @DisplayName("GET /api/statistics/daily returns daily metrics")
    void getDailyMetrics_returnsDailyMetrics() throws Exception {
        StatisticsDTO.DailyMetric metric = new StatisticsDTO.DailyMetric();
        metric.setDate(LocalDate.of(2024, 1, 1));
        metric.setOrders(10L);
        metric.setGmv(new BigDecimal("500.00"));
        metric.setUsers(5L);

        when(statisticsService.getDailyMetrics(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(metric));

        mockMvc.perform(get("/api/statistics/daily")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orders", is(10)))
            .andExpect(jsonPath("$[0].gmv", is(500.00)))
            .andExpect(jsonPath("$[0].users", is(5)));
    }

    @Test
    @DisplayName("GET /api/statistics/users/growth returns user growth data")
    void getUserGrowth_returnsGrowthData() throws Exception {
        Map<String, Object> day1 = new HashMap<>();
        day1.put("date", java.sql.Date.valueOf(LocalDate.of(2024, 1, 1)));
        day1.put("users", 5L);

        when(statisticsService.getUserGrowthData(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(day1));

        mockMvc.perform(get("/api/statistics/users/growth")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].users", is(5)));
    }

    @Test
    @DisplayName("GET /api/statistics/summary returns quick summary")
    void getQuickSummary_returnsSummary() throws Exception {
        StatisticsDTO metrics = new StatisticsDTO();
        metrics.setTotalOrders(100L);
        metrics.setTotalGMV(new BigDecimal("5000.00"));
        metrics.setTotalUsers(25L);
        metrics.setAverageOrderValue(new BigDecimal("50.00"));

        StatisticsDTO.DailyMetric todayMetric = new StatisticsDTO.DailyMetric();
        todayMetric.setDate(LocalDate.now());
        todayMetric.setOrders(15L);
        todayMetric.setGmv(new BigDecimal("750.00"));
        todayMetric.setUsers(5L);

        when(statisticsService.getOverviewMetrics(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(metrics);
        when(statisticsService.getDailyMetrics(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(todayMetric));

        mockMvc.perform(get("/api/statistics/summary")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.todayOrders", is(15)))
            .andExpect(jsonPath("$.todayGMV", is(750.00)))
            .andExpect(jsonPath("$.todayNewUsers", is(5)))
            .andExpect(jsonPath("$.weekOrders", is(100)))
            .andExpect(jsonPath("$.weekGMV", is(5000.00)));
    }

    @Test
    @DisplayName("GET /api/statistics/export returns export data")
    void exportData_returnsExportData() throws Exception {
        StatisticsDTO.DailyMetric metric = new StatisticsDTO.DailyMetric();
        metric.setDate(LocalDate.of(2024, 1, 1));
        metric.setOrders(10L);
        metric.setGmv(new BigDecimal("500.00"));
        metric.setUsers(5L);

        when(statisticsService.getDailyMetrics(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(metric));

        mockMvc.perform(get("/api/statistics/export")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-07")
                .param("format", "csv")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.format", is("csv")))
            .andExpect(jsonPath("$.startDate", is("2024-01-01")))
            .andExpect(jsonPath("$.endDate", is("2024-01-07")))
            .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
