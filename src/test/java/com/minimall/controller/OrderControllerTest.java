package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.OrderDTO;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.service.JwtService;
import com.minimall.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void getUserOrders_returnsOrdersForCurrentUser() throws Exception {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(99.99));

        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.findByUserId("user-1")).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/user/user-1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserOrders_whenNotCurrentUser_returns403() throws Exception {
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(get("/api/orders/user/other-user"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrder_returnsOrderWhenExists() throws Exception {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(99.99));

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);

        mockMvc.perform(get("/api/orders/order-1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getOrder_whenNotCurrentUser_returnsForbidden() throws Exception {
        User user = new User();
        user.setId("other-user");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(get("/api/orders/order-1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createOrder_returnsCreatedOrder() throws Exception {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("new-order");
        order.setOrderNo("ORD-NEW");
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(199.99));

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.create(eq("user-1"), any())).thenReturn(order);

        String requestBody = """
            {"userId":"user-1","items":[{"product":{"id":"prod-1"},"quantity":1,"price":99.99}]}
            """;

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createOrder_whenNotCurrentUser_returnsForbidden() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        String requestBody = """
            {"userId":"other-user","items":[{"product":{"id":"prod-1"},"quantity":1,"price":99.99}]}
            """;

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateStatus_returnsUpdatedOrder() throws Exception {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.updateStatus("order-1", Order.Status.SHIPPED)).thenReturn(order);

        mockMvc.perform(patch("/api/orders/order-1/status")
                .with(csrf())
                .param("status", "SHIPPED"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void payOrder_returnsPaidOrder() throws Exception {
        User user = new User();
        user.setId("user-1");
        Order order = new Order();
        order.setId("order-1");
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);
        order.setPayStatus(Order.PayStatus.PAID);

        when(orderService.findById("order-1")).thenReturn(order);
        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(orderService.pay("order-1", "TRADE-123")).thenReturn(order);

        mockMvc.perform(patch("/api/orders/order-1/pay")
                .with(csrf())
                .param("tradeNo", "TRADE-123"))
            .andExpect(status().isOk());
    }
}