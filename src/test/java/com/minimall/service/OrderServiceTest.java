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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private User mockUser;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, userService, productService,
                subscribeService, pointService, memberService
        );
        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setOpenid("openid-abc");
        mockUser.setNickname("TestUser");
        mockProduct = new Product();
        mockProduct.setId("prod-1");
        mockProduct.setName("TestProduct");
        mockProduct.setPrice(BigDecimal.valueOf(49.99));
    }

    @Test
    void findById_returnsOrder_whenExists() {
        Order order = new Order();
        order.setId("order-1");
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        Order result = orderService.findById("order-1");

        assertNotNull(result);
        assertEquals("order-1", result.getId());
        verify(orderRepository).findById("order-1");
    }

    @Test
    void findById_throws_whenNotFound() {
        when(orderRepository.findById("not-found")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.findById("not-found"));
    }

    @Test
    void findByOrderNo_returnsOrder_whenExists() {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORDER-123");
        when(orderRepository.findByOrderNo("ORDER-123")).thenReturn(Optional.of(order));

        Order result = orderService.findByOrderNo("ORDER-123");

        assertNotNull(result);
        assertEquals("ORDER-123", result.getOrderNo());
        verify(orderRepository).findByOrderNo("ORDER-123");
    }

    @Test
    void findByOrderNo_throws_whenNotFound() {
        when(orderRepository.findByOrderNo("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.findByOrderNo("unknown"));
    }

    @Test
    void create_calculatesTotalAndSaves() {
        when(userService.findById("user-123")).thenReturn(mockUser);
        when(productService.findById("prod-1")).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product product = new Product();
        product.setId("prod-1");
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order result = orderService.create("user-123", List.of(item));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(99.98), result.getTotalAmount());
        verify(subscribeService).sendOrderCreatedMessage(any(Order.class), eq(mockUser));
    }

    @Test
    void updateStatus_toShipped_sendsNotification() {
        Order order = new Order();
        order.setId("order-1");
        order.setUser(mockUser);
        order.setStatus(Order.Status.PAID);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus("order-1", Order.Status.SHIPPED);

        verify(subscribeService).sendOrderShippedMessage(any(Order.class), eq(mockUser), eq(""));
    }

    @Test
    void updateStatus_toCompleted_awardsPoints() {
        Order order = new Order();
        order.setId("order-1");
        order.setUser(mockUser);
        order.setStatus(Order.Status.PAID);
        order.setOrderNo("ORDER-123");
        order.setTotalAmount(BigDecimal.valueOf(100));
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus("order-1", Order.Status.COMPLETED);

        verify(pointService).earnOrderPoints("user-123", "ORDER-123", BigDecimal.valueOf(100));
    }

    @Test
    void pay_setsPayStatusAndSendsNotification() {
        Order order = new Order();
        order.setId("order-1");
        order.setUser(mockUser);
        order.setStatus(Order.Status.PENDING);
        order.setPayStatus(Order.PayStatus.UNPAID);
        order.setTotalAmount(BigDecimal.valueOf(99.99));
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.pay("order-1", "TRADE-123");

        assertEquals(Order.PayStatus.PAID, result.getPayStatus());
        assertEquals(Order.Status.PAID, result.getStatus());
        assertEquals("TRADE-123", result.getTradeNo());
        assertNotNull(result.getPayTime());
        verify(memberService).updateTotalSpent("user-123", BigDecimal.valueOf(99.99));
        verify(subscribeService).sendOrderPaidMessage(any(Order.class), eq(mockUser));
    }
}