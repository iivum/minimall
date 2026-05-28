package com.minimall.service;

import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.Product;
import com.minimall.model.User;
import com.minimall.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        orderService = new OrderService(
            orderRepository, userService, productService,
            subscribeService, pointService, memberService
        );
    }

    @Test
    void findByUserId_returnsOrdersForUser() {
        User user = new User();
        user.setId("user-1");
        Order order1 = new Order();
        order1.setOrderNo("ORD-001");
        order1.setUser(user);
        Order order2 = new Order();
        order2.setOrderNo("ORD-002");
        order2.setUser(user);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
            .thenReturn(Arrays.asList(order1, order2));

        List<Order> result = orderService.findByUserId("user-1");

        assertEquals(2, result.size());
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc("user-1");
    }

    @Test
    void findAll_returnsAllOrders() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        List<Order> result = orderService.findAll();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void findById_returnsOrderWhenExists() {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        Order result = orderService.findById("order-1");

        assertNotNull(result);
        assertEquals("order-1", result.getId());
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(orderRepository.findById("not-found")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.findById("not-found"));
    }

    @Test
    void findByOrderNo_returnsOrderWhenExists() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        when(orderRepository.findByOrderNo("ORD-001")).thenReturn(Optional.of(order));

        Order result = orderService.findByOrderNo("ORD-001");

        assertNotNull(result);
        assertEquals("ORD-001", result.getOrderNo());
    }

    @Test
    void findByOrderNo_throwsWhenNotFound() {
        when(orderRepository.findByOrderNo("NOT-EXIST")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.findByOrderNo("NOT-EXIST"));
    }

    @Test
    void create_savesOrderWithItems() {
        User user = new User();
        user.setId("user-1");
        Product product = new Product();
        product.setId("prod-1");
        product.setPrice(BigDecimal.valueOf(10.00));
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        when(userService.findById("user-1")).thenReturn(user);
        when(productService.findByIds(List.of("prod-1"))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("new-order-id");
            return o;
        });

        Order result = orderService.create("user-1", Arrays.asList(item));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(20.00), result.getTotalAmount());
        verify(subscribeService).sendOrderCreatedMessage(any(Order.class), eq(user));
    }

    @Test
    void updateStatus_shipsOrderAndSendsMessage() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PAID);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateStatus("order-1", Order.Status.SHIPPED);

        assertEquals(Order.Status.SHIPPED, result.getStatus());
        verify(subscribeService).sendOrderShippedMessage(any(Order.class), eq(user), eq(""));
    }

    @Test
    void updateStatus_completedOrderEarnsPoints() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PAID);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setOrderNo("ORD-001");
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateStatus("order-1", Order.Status.COMPLETED);

        verify(pointService).earnOrderPoints("user-1", "ORD-001", BigDecimal.valueOf(100.00));
    }

    @Test
    void pay_updatesPayStatusAndSendsMessage() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(50.00));
        order.setStatus(Order.Status.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.pay("order-1", "TRADE-123");

        assertEquals(Order.PayStatus.PAID, result.getPayStatus());
        assertEquals(Order.Status.PAID, result.getStatus());
        assertEquals("TRADE-123", result.getTradeNo());
        assertNotNull(result.getPayTime());
        verify(subscribeService).sendOrderPaidMessage(any(Order.class), eq(user));
        verify(memberService).updateTotalSpent("user-1", BigDecimal.valueOf(50.00));
    }
}