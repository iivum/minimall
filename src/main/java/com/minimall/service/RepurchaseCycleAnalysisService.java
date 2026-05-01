package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RepurchaseCycleAnalysisService {
    private final OrderRepository orderRepository;

    public RepurchaseCycleAnalysisService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public long calculateAverageRepurchaseCycle(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (orders.size() < 2) {
            return 30;
        }

        long totalDays = 0;
        Instant previousTime = null;

        for (Order order : orders) {
            if (previousTime != null) {
                long days = ChronoUnit.DAYS.between(order.getCreatedAt(), previousTime);
                totalDays += Math.abs(days);
            }
            previousTime = order.getCreatedAt();
        }

        return totalDays / (orders.size() - 1);
    }

    public Instant calculateNextOptimalReminderTime(String userId) {
        long avgCycle = calculateAverageRepurchaseCycle(userId);
        long reminderAdvanceDays = Math.max(1, avgCycle / 10);
        return Instant.now().plus(Math.max(1, avgCycle - reminderAdvanceDays), ChronoUnit.DAYS);
    }

    public PurchaseFrequency getPurchaseFrequency(String userId) {
        long avgCycle = calculateAverageRepurchaseCycle(userId);

        if (avgCycle <= 7) {
            return PurchaseFrequency.HIGH;
        } else if (avgCycle <= 30) {
            return PurchaseFrequency.MEDIUM;
        } else {
            return PurchaseFrequency.LOW;
        }
    }

    public enum PurchaseFrequency {
        HIGH,
        MEDIUM,
        LOW
    }
}