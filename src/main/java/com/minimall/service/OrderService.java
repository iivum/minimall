package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.User;
import com.minimall.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, UserService userService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public List<Order> findByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order findById(String id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Order findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderNo));
    }

    @Transactional
    public Order create(String userId, List<OrderItem> items) {
        User user = userService.findById(userId);
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString());
        order.setUser(user);
        
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            item.setOrder(order);
            item.setPrice(productService.findById(item.getProduct().getId()).getPrice());
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setItems(items);
        order.setTotalAmount(total);
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(String id, Order.Status status) {
        Order order = findById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public Order pay(String id, String tradeNo) {
        Order order = findById(id);
        order.setPayStatus(Order.PayStatus.PAID);
        order.setStatus(Order.Status.PAID);
        order.setPayTime(Instant.now());
        order.setTradeNo(tradeNo);
        return orderRepository.save(order);
    }
}
