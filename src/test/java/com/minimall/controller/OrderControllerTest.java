package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.User;
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
    private com.minimall.service.JwtService jwtService;

    private User createTestUser(String id) {
        User user = new User();
        user.setId(id);
        user.setOpenid("test-openid");
        user.setNickname("Test User");
        user.setPhone("1234567890");
        return user;
    }

    private Order createTestOrder(String id, String userId) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("ORDER-123");
        order.setUser(createTestUser(userId));
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setStatus(Order.Status.PENDING);
        order.setPayStatus(Order.PayStatus.UNPAID);
        return order;
    }

    @Test
    @WithMockUser
    void getUserOrders_returnsOrderList_whenAuthorized() throws Exception {
        String userId = "user-123";
        Order order = createTestOrder("order-1", userId);
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);
        when(orderService.findByUserId(userId)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/user/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("order-1"))
            .andExpect(jsonPath("$[0].orderNo").value("ORDER-123"));
    }

    @Test
    @WithMockUser
    void getUserOrders_throwsForbidden_whenNotOwner() throws Exception {
        String userId = "user-123";
        when(securityUtils.isCurrentUser(userId)).thenReturn(false);

        mockMvc.perform(get("/api/orders/user/{userId}", userId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrder_returnsOrder_whenAuthorized() throws Exception {
        String orderId = "order-123";
        String userId = "user-123";
        Order order = createTestOrder(orderId, userId);
        when(orderService.findById(orderId)).thenReturn(order);
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.orderNo").value("ORDER-123"));
    }

    @Test
    @WithMockUser
    void getOrder_throwsForbidden_whenNotOwner() throws Exception {
        String orderId = "order-123";
        Order order = createTestOrder(orderId, "other-user");
        when(orderService.findById(orderId)).thenReturn(order);
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrderByNo_returnsOrder_whenAuthorized() throws Exception {
        String orderNo = "ORDER-456";
        String userId = "user-123";
        Order order = createTestOrder("order-789", userId);
        order.setOrderNo(orderNo);
        when(orderService.findByOrderNo(orderNo)).thenReturn(order);
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);

        mockMvc.perform(get("/api/orders/no/{orderNo}", orderNo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNo").value(orderNo));
    }

    @Test
    @WithMockUser
    void getOrderByNo_throwsForbidden_whenNotOwner() throws Exception {
        String orderNo = "ORDER-456";
        Order order = createTestOrder("order-789", "other-user");
        order.setOrderNo(orderNo);
        when(orderService.findByOrderNo(orderNo)).thenReturn(order);
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(get("/api/orders/no/{orderNo}", orderNo))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createOrder_returnsOrder_whenAuthorized() throws Exception {
        String userId = "user-123";
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);
        when(orderService.create(eq(userId), any())).thenReturn(createTestOrder("new-order", userId));

        String requestBody = """
            {
                "userId": "user-123",
                "items": []
            }
            """;

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("new-order"));
    }

    @Test
    @WithMockUser
    void createOrder_throwsForbidden_whenNotOwner() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        String requestBody = """
            {
                "userId": "user-123",
                "items": []
            }
            """;

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateStatus_returnsOrder_whenAuthorized() throws Exception {
        String orderId = "order-123";
        String userId = "user-123";
        Order order = createTestOrder(orderId, userId);
        order.setStatus(Order.Status.SHIPPED);
        when(orderService.findById(orderId)).thenReturn(order);
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);
        when(orderService.updateStatus(orderId, Order.Status.SHIPPED)).thenReturn(order);

        mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                .with(csrf())
                .param("status", "SHIPPED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @WithMockUser
    void updateStatus_throwsForbidden_whenNotOwner() throws Exception {
        String orderId = "order-123";
        Order order = createTestOrder(orderId, "other-user");
        when(orderService.findById(orderId)).thenReturn(order);
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                .with(csrf())
                .param("status", "SHIPPED"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void payOrder_returnsOrder_whenAuthorized() throws Exception {
        String orderId = "order-123";
        String userId = "user-123";
        String tradeNo = "TRADE-12345";
        Order order = createTestOrder(orderId, userId);
        order.setPayStatus(Order.PayStatus.PAID);
        when(orderService.findById(orderId)).thenReturn(order);
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);
        when(orderService.pay(orderId, tradeNo)).thenReturn(order);

        mockMvc.perform(patch("/api/orders/{id}/pay", orderId)
                .with(csrf())
                .param("tradeNo", tradeNo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payStatus").value("PAID"));
    }

    @Test
    @WithMockUser
    void payOrder_throwsForbidden_whenNotOwner() throws Exception {
        String orderId = "order-123";
        Order order = createTestOrder(orderId, "other-user");
        when(orderService.findById(orderId)).thenReturn(order);
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(patch("/api/orders/{id}/pay", orderId)
                .with(csrf())
                .param("tradeNo", "TRADE-12345"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getUserOrders_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/orders/user/user-123"))
            .andExpect(status().isUnauthorized());
    }
}
