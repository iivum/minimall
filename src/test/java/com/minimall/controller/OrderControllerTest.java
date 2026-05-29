package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.DtoMapper;
import com.minimall.dto.OrderDTO;
import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.User;
import com.minimall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private SecurityUtils securityUtils;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService, securityUtils);
    }

    @Test
    @DisplayName("getUserOrders returns orders for current user")
    void getUserOrders_returnsOrders() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("100.00"));

        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.findByUserId("user-1")).thenReturn(List.of(order));

        ResponseEntity<List<OrderDTO>> response = orderController.getUserOrders("user-1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("getUserOrders throws when accessing other user orders")
    void getUserOrders_throwsWhenNotOwner() {
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        assertThrows(com.minimall.exception.UnauthorizedException.class,
            () -> orderController.getUserOrders("other-user"));
    }

    @Test
    @DisplayName("getOrder returns order when owner")
    void getOrder_returnsOrder() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("100.00"));

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);

        ResponseEntity<OrderDTO> response = orderController.getOrder("order-1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("getOrder throws when not owner")
    void getOrder_throwsWhenNotOwner() {
        User user = new User();
        user.setId("other-user");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        assertThrows(com.minimall.exception.UnauthorizedException.class,
            () -> orderController.getOrder("order-1"));
    }

    @Test
    @DisplayName("createOrder creates order for current user")
    void createOrder_success() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("new-order");
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("200.00"));

        OrderItem item = new OrderItem();
        item.setQuantity(2);

        OrderController.CreateOrderRequest request =
            new OrderController.CreateOrderRequest();
        request.userId = "user-1";
        request.items = List.of(item);

        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.create("user-1", request.items)).thenReturn(order);

        ResponseEntity<OrderDTO> response = orderController.createOrder(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("createOrder throws when creating for different user")
    void createOrder_throwsWhenNotOwner() {
        OrderController.CreateOrderRequest request =
            new OrderController.CreateOrderRequest();
        request.userId = "other-user";
        request.items = List.of();

        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        assertThrows(com.minimall.exception.UnauthorizedException.class,
            () -> orderController.createOrder(request));
    }

    @Test
    @DisplayName("updateStatus updates order status")
    void updateStatus_success() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PAID);

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.updateStatus("order-1", Order.Status.SHIPPED)).thenReturn(order);

        ResponseEntity<OrderDTO> response =
            orderController.updateStatus("order-1", Order.Status.SHIPPED);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("payOrder marks order as paid")
    void payOrder_success() {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PAID);

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.pay("order-1", "TRADE-123")).thenReturn(order);

        ResponseEntity<OrderDTO> response =
            orderController.payOrder("order-1", "TRADE-123");

        assertEquals(200, response.getStatusCode().value());
    }
}