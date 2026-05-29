package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.model.Product;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(orderRepository, productRepository);
    }

    @Test
    void getDashboardStats_returnsCorrectStats() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByPayStatus(Order.PayStatus.PAID)).thenReturn(80L);
        when(orderRepository.sumTotalAmountByPayStatus(Order.PayStatus.PAID)).thenReturn(BigDecimal.valueOf(10000));
        when(productRepository.count()).thenReturn(50L);
        when(productRepository.countByActiveTrue()).thenReturn(40L);

        StatsService.DashboardStats result = statsService.getDashboardStats();

        assertEquals(100, result.totalOrders());
        assertEquals(80, result.paidOrders());
        assertEquals(BigDecimal.valueOf(10000), result.totalRevenue());
        assertEquals(50, result.totalProducts());
        assertEquals(40, result.activeProducts());
    }

    @Test
    void getDashboardStats_handlesNullRevenue() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.countByPayStatus(any())).thenReturn(0L);
        when(orderRepository.sumTotalAmountByPayStatus(any())).thenReturn(null);
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.countByActiveTrue()).thenReturn(0L);

        StatsService.DashboardStats result = statsService.getDashboardStats();

        assertEquals(BigDecimal.ZERO, result.totalRevenue());
    }

    @Test
    void getOrdersTrend_returnsTrendForSpecifiedDays() {
        Order order = new Order();
        order.setId("order-1");
        order.setTotalAmount(BigDecimal.valueOf(500));
        order.setPayStatus(Order.PayStatus.PAID);
        order.setPayTime(Instant.now());

        when(orderRepository.findByPayStatus(Order.PayStatus.PAID)).thenReturn(List.of(order));

        List<StatsService.OrdersTrendEntry> result = statsService.getOrdersTrend(7);

        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).date());
        assertNotNull(result.get(0).orderCount());
        assertNotNull(result.get(0).revenue());
    }

    @Test
    void getOrdersTrend_returnsEmptyListWhenNoOrders() {
        when(orderRepository.findByPayStatus(Order.PayStatus.PAID)).thenReturn(Collections.emptyList());

        List<StatsService.OrdersTrendEntry> result = statsService.getOrdersTrend(7);

        assertEquals(7, result.size());
    }
}