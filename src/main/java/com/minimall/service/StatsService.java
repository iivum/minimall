package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public StatsService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public DashboardStats getDashboardStats() {
        long totalOrders = orderRepository.count();
        long paidOrders = orderRepository.countByPayStatus(Order.PayStatus.PAID);
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByPayStatus(Order.PayStatus.PAID);
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByActiveTrue();

        return new DashboardStats(totalOrders, paidOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalProducts, activeProducts);
    }

    public List<OrdersTrendEntry> getOrdersTrend(int days) {
        LocalDate now = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        List<Order> orders = orderRepository.findByPayStatus(Order.PayStatus.PAID);

        Map<LocalDate, BigDecimal> dailyRevenue = new HashMap<>();
        Map<LocalDate, Long> dailyOrders = new HashMap<>();

        for (int i = 0; i < days; i++) {
            dailyRevenue.put(now.minusDays(i), BigDecimal.ZERO);
            dailyOrders.put(now.minusDays(i), 0L);
        }

        for (Order order : orders) {
            LocalDate orderDate = order.getPayTime() != null
                    ? order.getPayTime().atZone(zone).toLocalDate()
                    : order.getCreatedAt().atZone(zone).toLocalDate();

            if (orderDate.isAfter(now.minusDays(days))) {
                dailyRevenue.merge(orderDate, order.getTotalAmount(), BigDecimal::add);
                dailyOrders.merge(orderDate, 1L, Long::sum);
            }
        }

        return dailyRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new OrdersTrendEntry(e.getKey().toString(), dailyOrders.get(e.getKey()), e.getValue()))
                .toList();
    }

    public record DashboardStats(long totalOrders, long paidOrders, BigDecimal totalRevenue,
                                  long totalProducts, long activeProducts) {}

    public record OrdersTrendEntry(String date, Long orderCount, BigDecimal revenue) {}
}