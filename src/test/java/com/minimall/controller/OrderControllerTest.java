package com.minimall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.config.SecurityUtils;
import com.minimall.model.Order;
import com.minimall.model.OrderItem;
import com.minimall.model.Product;
import com.minimall.model.User;
import com.minimall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setNickname("TestUser");

        testOrder = new Order();
        testOrder.setId("order-456");
        testOrder.setOrderNo("ORDER-001");
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.00));
        testOrder.setStatus(Order.Status.PENDING);
        testOrder.setPayStatus(Order.PayStatus.UNPAID);
    }

    @Test
    @WithMockUser
    void getUserOrders_withValidUserId_returnsOrders() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findByUserId("user-123")).thenReturn(List.of(testOrder));

        mockMvc.perform(get("/api/orders/user/user-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("order-456"))
            .andExpect(jsonPath("$[0].orderNo").value("ORDER-001"));
    }

    @Test
    @WithMockUser
    void getUserOrders_withDifferentUserId_throwsUnauthorized() throws Exception {
        when(securityUtils.isCurrentUser("user-999")).thenReturn(false);

        mockMvc.perform(get("/api/orders/user/user-999"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrder_withValidId_returnsOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findById("order-456")).thenReturn(testOrder);

        mockMvc.perform(get("/api/orders/order-456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("order-456"))
            .andExpect(jsonPath("$.orderNo").value("ORDER-001"));
    }

    @Test
    @WithMockUser
    void getOrder_withUnauthorizedUser_throwsUnauthorized() throws Exception {
        when(orderService.findById("order-456")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(get("/api/orders/order-456"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrderByNo_withValidOrderNo_returnsOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findByOrderNo("ORDER-001")).thenReturn(testOrder);

        mockMvc.perform(get("/api/orders/no/ORDER-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("order-456"));
    }

    @Test
    @WithMockUser
    void getOrderByNo_withUnauthorizedUser_throwsUnauthorized() throws Exception {
        when(orderService.findByOrderNo("ORDER-001")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(get("/api/orders/no/ORDER-001"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createOrder_withValidRequest_returnsCreatedOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);

        Product product = new Product();
        product.setId("prod-001");
        product.setPrice(BigDecimal.valueOf(50.00));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.userId = "user-123";
        request.items = List.of(item);

        when(orderService.create(eq("user-123"), any())).thenReturn(testOrder);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("order-456"));
    }

    @Test
    @WithMockUser
    void createOrder_withUnauthorizedUser_throwsUnauthorized() throws Exception {
        when(securityUtils.isCurrentUser("user-999")).thenReturn(false);

        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.userId = "user-999";

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateStatus_withValidRequest_returnsUpdatedOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findById("order-456")).thenReturn(testOrder);

        Order updatedOrder = new Order();
        updatedOrder.setId("order-456");
        updatedOrder.setStatus(Order.Status.SHIPPED);
        when(orderService.updateStatus("order-456", Order.Status.SHIPPED)).thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/orders/order-456/status")
                .with(csrf())
                .param("status", "SHIPPED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @WithMockUser
    void updateStatus_withUnauthorizedUser_throwsUnauthorized() throws Exception {
        when(orderService.findById("order-456")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(patch("/api/orders/order-456/status")
                .with(csrf())
                .param("status", "SHIPPED"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void payOrder_withValidRequest_returnsPaidOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findById("order-456")).thenReturn(testOrder);

        Order paidOrder = new Order();
        paidOrder.setId("order-456");
        paidOrder.setPayStatus(Order.PayStatus.PAID);
        paidOrder.setStatus(Order.Status.PAID);
        when(orderService.pay("order-456", "TRADE-123")).thenReturn(paidOrder);

        mockMvc.perform(patch("/api/orders/order-456/pay")
                .with(csrf())
                .param("tradeNo", "TRADE-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.payStatus").value("PAID"));
    }

    @Test
    @WithMockUser
    void payOrder_withUnauthorizedUser_throwsUnauthorized() throws Exception {
        when(orderService.findById("order-456")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(patch("/api/orders/order-456/pay")
                .with(csrf())
                .param("tradeNo", "TRADE-123"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getUserOrders_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/orders/user/user-123"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrder_withoutAuthentication_returnsUnauthorized() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.userId = "user-123";

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }
}