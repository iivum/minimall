package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.Product;
import com.minimall.model.User;
import com.minimall.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private WeChatSubscribeService subscribeService;

    @Mock
    private PointService pointService;

    @Mock
    private MemberService memberService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, userService, productService,
            subscribeService, pointService, memberService);
    }

    @Test
    @DisplayName("findByUserId returns orders for user")
    void findByUserId_returnsOrders() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);

        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(order));

        List<Order> result = orderService.findByUserId("user-1");

        assertEquals(1, result.size());
        assertEquals("order-1", result.get(0).getId());
    }

    @Test
    @DisplayName("findById returns order when exists")
    void findById_existingOrder_returnsOrder() {
        Order order = new Order();
        order.setId("order-1");

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        Order result = orderService.findById("order-1");

        assertNotNull(result);
        assertEquals("order-1", result.getId());
    }

    @Test
    @DisplayName("findById throws when order not found")
    void findById_notFound_throwsException() {
        when(orderRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.findById("invalid"));
    }

    @Test
    @DisplayName("create creates order with items")
    void create_withItems_createsOrder() {
        User user = new User();
        user.setId("user-1");

        Product product = new Product();
        product.setId("prod-1");
        product.setPrice(new BigDecimal("50"));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        when(userService.findById("user-1")).thenReturn(user);
        when(productService.findById("prod-1")).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId("order-1");
            return saved;
        });

        Order result = orderService.create("user-1", List.of(item));

        assertNotNull(result);
        assertEquals(new BigDecimal("100"), result.getTotalAmount());
        verify(subscribeService).sendOrderCreatedMessage(any(), eq(user));
    }

    @Test
    @DisplayName("updateStatus to SHIPPED sends notification")
    void updateStatus_shipped_sendsNotification() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.updateStatus("order-1", Order.Status.SHIPPED);

        assertEquals(Order.Status.SHIPPED, result.getStatus());
        verify(subscribeService).sendOrderShippedMessage(any(), eq(user), anyString());
    }

    @Test
    @DisplayName("updateStatus to COMPLETED awards points")
    void updateStatus_completed_awardsPoints() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);
        order.setTotalAmount(new BigDecimal("1000"));

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.updateStatus("order-1", Order.Status.COMPLETED);

        verify(pointService).earnOrderPoints(eq("user-1"), any(), eq(new BigDecimal("1000")));
    }

    @Test
    @DisplayName("pay updates order and calls member service")
    void pay_updatesOrderAndMember() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);
        order.setTotalAmount(new BigDecimal("100"));

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.pay("order-1", "TRADE123");

        assertEquals(Order.PayStatus.PAID, result.getPayStatus());
        assertEquals(Order.Status.PAID, result.getStatus());
        assertEquals("TRADE123", result.getTradeNo());
        verify(subscribeService).sendOrderPaidMessage(any(), eq(user));
        verify(memberService).updateTotalSpent("user-1", new BigDecimal("100"));
    }
}