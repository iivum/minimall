package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(orderRepository, productRepository);
    }

    @Test
    @DisplayName("getDashboardStats returns all statistics")
    void getDashboardStats_success() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByPayStatus(Order.PayStatus.PAID)).thenReturn(80L);
        when(orderRepository.sumTotalAmountByPayStatus(Order.PayStatus.PAID))
            .thenReturn(new BigDecimal("10000.00"));
        when(productRepository.count()).thenReturn(50L);
        when(productRepository.countByActiveTrue()).thenReturn(40L);

        StatsService.DashboardStats stats = statsService.getDashboardStats();

        assertEquals(100L, stats.totalOrders());
        assertEquals(80L, stats.paidOrders());
        assertEquals(new BigDecimal("10000.00"), stats.totalRevenue());
        assertEquals(50L, stats.totalProducts());
        assertEquals(40L, stats.activeProducts());
    }

    @Test
    @DisplayName("getDashboardStats handles null revenue")
    void getDashboardStats_nullRevenue() {
        when(orderRepository.count()).thenReturn(10L);
        when(orderRepository.countByPayStatus(Order.PayStatus.PAID)).thenReturn(5L);
        when(orderRepository.sumTotalAmountByPayStatus(Order.PayStatus.PAID)).thenReturn(null);
        when(productRepository.count()).thenReturn(20L);
        when(productRepository.countByActiveTrue()).thenReturn(15L);

        StatsService.DashboardStats stats = statsService.getDashboardStats();

        assertEquals(BigDecimal.ZERO, stats.totalRevenue());
    }

    @Test
    @DisplayName("getOrdersTrend returns trend entries for specified days")
    void getOrdersTrend_returnsEntries() {
        Order order1 = new Order();
        order1.setPayTime(Instant.now());
        order1.setTotalAmount(new BigDecimal("100.00"));
        Order order2 = new Order();
        order2.setPayTime(Instant.now().minus(1, ChronoUnit.DAYS));
        order2.setTotalAmount(new BigDecimal("200.00"));

        when(orderRepository.findByPayStatus(Order.PayStatus.PAID))
            .thenReturn(List.of(order1, order2));

        List<StatsService.OrdersTrendEntry> trend = statsService.getOrdersTrend(7);

        assertNotNull(trend);
        assertEquals(7, trend.size());
    }

    @Test
    @DisplayName("getOrdersTrend handles empty order list")
    void getOrdersTrend_emptyOrders() {
        when(orderRepository.findByPayStatus(Order.PayStatus.PAID))
            .thenReturn(Collections.emptyList());

        List<StatsService.OrdersTrendEntry> trend = statsService.getOrdersTrend(7);

        assertNotNull(trend);
        assertEquals(7, trend.size());
        trend.forEach(entry -> {
            assertEquals(0L, entry.orderCount());
            assertEquals(BigDecimal.ZERO, entry.revenue());
        });
    }

    @Test
    @DisplayName("getOrdersTrend handles order with only createdAt")
    void getOrdersTrend_onlyCreatedAt() {
        Order order = new Order();
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPayTime(Instant.now());

        when(orderRepository.findByPayStatus(Order.PayStatus.PAID))
            .thenReturn(List.of(order));

        List<StatsService.OrdersTrendEntry> trend = statsService.getOrdersTrend(7);

        assertNotNull(trend);
        assertEquals(7, trend.size());
    }
}