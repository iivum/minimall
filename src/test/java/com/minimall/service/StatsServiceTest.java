package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
    void getDashboardStats_returnsCorrectStats() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByPayStatus(Order.PayStatus.PAID)).thenReturn(80L);
        when(orderRepository.sumTotalAmountByPayStatus(Order.PayStatus.PAID)).thenReturn(BigDecimal.valueOf(5000.00));
        when(productRepository.count()).thenReturn(50L);
        when(productRepository.countByActiveTrue()).thenReturn(40L);

        StatsService.DashboardStats result = statsService.getDashboardStats();

        assertEquals(100L, result.totalOrders());
        assertEquals(80L, result.paidOrders());
        assertEquals(BigDecimal.valueOf(5000.00), result.totalRevenue());
        assertEquals(50L, result.totalProducts());
        assertEquals(40L, result.activeProducts());
    }

    @Test
    void getDashboardStats_handlesNullRevenue() {
        when(orderRepository.count()).thenReturn(10L);
        when(orderRepository.countByPayStatus(Order.PayStatus.PAID)).thenReturn(5L);
        when(orderRepository.sumTotalAmountByPayStatus(Order.PayStatus.PAID)).thenReturn(null);
        when(productRepository.count()).thenReturn(20L);
        when(productRepository.countByActiveTrue()).thenReturn(15L);

        StatsService.DashboardStats result = statsService.getDashboardStats();

        assertEquals(BigDecimal.ZERO, result.totalRevenue());
    }

    @Test
    void getOrdersTrend_returnsTrendEntries() {
        when(orderRepository.findByPayStatus(Order.PayStatus.PAID)).thenReturn(List.of());

        List<StatsService.OrdersTrendEntry> result = statsService.getOrdersTrend(7);

        assertNotNull(result);
        assertEquals(7, result.size());
    }
}