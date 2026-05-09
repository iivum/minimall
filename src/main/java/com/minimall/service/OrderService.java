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
    private final WeChatSubscribeService subscribeService;
    private final PointService pointService;
    private final MemberService memberService;

    public OrderService(OrderRepository orderRepository,
                       UserService userService,
                       ProductService productService,
                       WeChatSubscribeService subscribeService,
                       PointService pointService,
                       MemberService memberService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.subscribeService = subscribeService;
        this.pointService = pointService;
        this.memberService = memberService;
    }

    public List<Order> findByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
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

        Order savedOrder = orderRepository.save(order);

        subscribeService.sendOrderCreatedMessage(savedOrder, user);

        return savedOrder;
    }

    @Transactional
    public Order updateStatus(String id, Order.Status status) {
        Order order = findById(id);
        Order.Status oldStatus = order.getStatus();
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        if (status == Order.Status.SHIPPED && oldStatus != Order.Status.SHIPPED) {
            subscribeService.sendOrderShippedMessage(savedOrder, order.getUser(), "");
        } else if (status == Order.Status.COMPLETED && oldStatus != Order.Status.COMPLETED) {
            subscribeService.sendOrderCompletedMessage(savedOrder, order.getUser());
            String userId = order.getUser().getId();
            pointService.earnOrderPoints(userId, order.getOrderNo(), order.getTotalAmount());
        }

        return savedOrder;
    }

    @Transactional
    public Order pay(String id, String tradeNo) {
        Order order = findById(id);
        order.setPayStatus(Order.PayStatus.PAID);
        order.setStatus(Order.Status.PAID);
        order.setPayTime(Instant.now());
        order.setTradeNo(tradeNo);
        Order savedOrder = orderRepository.save(order);

        subscribeService.sendOrderPaidMessage(savedOrder, order.getUser());

        memberService.updateTotalSpent(order.getUser().getId(), order.getTotalAmount());

        return savedOrder;
    }
}