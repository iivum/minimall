package com.minimall.controller;

import com.minimall.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @Mock
    private StatsService statsService;

    private StatsController controller;

    @BeforeEach
    void setUp() {
        controller = new StatsController(statsService);
    }

    @Test
    void dashboard_returnsDashboardStats() {
        StatsService.DashboardStats stats = new StatsService.DashboardStats(100, 50, BigDecimal.valueOf(200), 1000L, 800L);
        when(statsService.getDashboardStats()).thenReturn(stats);

        ResponseEntity<StatsService.DashboardStats> response = controller.dashboard();

        assertNotNull(response.getBody());
        assertEquals(100, response.getBody().totalOrders());
    }

    @Test
    void ordersTrend_returnsTrendData() {
        List<StatsService.OrdersTrendEntry> trend = List.of(
            new StatsService.OrdersTrendEntry("2024-01-01", 10L, BigDecimal.valueOf(100)));
        when(statsService.getOrdersTrend(30)).thenReturn(trend);

        ResponseEntity<?> response = controller.ordersTrend(30);

        assertNotNull(response.getBody());
    }
}
