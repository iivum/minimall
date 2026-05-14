package com.minimall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.config.SecurityUtils;
import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.Product;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private User mockUser() {
        User user = new User();
        user.setId("user-123");
        user.setOpenid("openid-abc");
        user.setNickname("TestUser");
        return user;
    }

    private Order mockOrder(String id, String userId) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("ORDER-" + id);
        User user = new User();
        user.setId(userId);
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(99.99));
        order.setStatus(Order.Status.PENDING);
        return order;
    }

    @Test
    @WithMockUser
    void getUserOrders_returnsOrderList() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findByUserId("user-123")).thenReturn(List.of(mockOrder("1", "user-123")));

        mockMvc.perform(get("/api/orders/user/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    @WithMockUser
    void getUserOrders_forbidsAccessToOtherUser() throws Exception {
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        mockMvc.perform(get("/api/orders/user/other-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrder_returnsOrder() throws Exception {
        Order order = mockOrder("order-1", "user-123");
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findById("order-1")).thenReturn(order);

        mockMvc.perform(get("/api/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-1"));
    }

    @Test
    @WithMockUser
    void getOrderByNo_returnsOrder() throws Exception {
        Order order = mockOrder("order-2", "user-123");
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findByOrderNo("ORDER-ORDER-2")).thenReturn(order);

        mockMvc.perform(get("/api/orders/no/ORDER-ORDER-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-2"));
    }

    @Test
    @WithMockUser
    void createOrder_createsAndReturnsOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);

        Product product = new Product();
        product.setId("prod-1");
        product.setPrice(BigDecimal.valueOf(49.99));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order order = mockOrder("new-order", "user-123");
        when(orderService.create(eq("user-123"), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(order);

        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.userId = "user-123";
        request.items = List.of(item);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-order"));
    }

    @Test
    @WithMockUser
    void updateStatus_updatesAndReturnsOrder() throws Exception {
        Order order = mockOrder("order-1", "user-123");
        order.setStatus(Order.Status.PAID);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findById("order-1")).thenReturn(order);
        when(orderService.updateStatus("order-1", Order.Status.PAID)).thenReturn(order);

        mockMvc.perform(patch("/api/orders/order-1/status")
                        .with(csrf())
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser
    void payOrder_marksAsPaid() throws Exception {
        Order order = mockOrder("order-1", "user-123");
        order.setPayStatus(Order.PayStatus.PAID);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findById("order-1")).thenReturn(order);
        when(orderService.pay("order-1", "TRADE-123")).thenReturn(order);

        mockMvc.perform(patch("/api/orders/order-1/pay")
                        .with(csrf())
                        .param("tradeNo", "TRADE-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payStatus").value("PAID"));
    }
}