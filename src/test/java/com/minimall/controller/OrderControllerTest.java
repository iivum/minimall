package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.exception.UnauthorizedException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setNickname("Test User");
        testUser.setMemberGrade("L1");
        testUser.setTotalSpent(BigDecimal.ZERO);

        testOrder = new Order();
        testOrder.setId("order-123");
        testOrder.setOrderNo("ORDER-001");
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(BigDecimal.valueOf(100));
        testOrder.setStatus(Order.Status.PENDING);
        testOrder.setPayStatus(Order.PayStatus.UNPAID);
    }

    @Test
    @WithMockUser
    void getUserOrders_returnsOrderList() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.findByUserId("user-123")).thenReturn(List.of(testOrder));

        mockMvc.perform(get("/api/orders/user/user-123"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserOrders_throwsUnauthorized_whenNotCurrentUser() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(get("/api/orders/user/user-123"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrder_returnsOrder() throws Exception {
        when(orderService.findById("order-123")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);

        mockMvc.perform(get("/api/orders/order-123"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getOrder_throwsUnauthorized_whenNotOwner() throws Exception {
        when(orderService.findById("order-123")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(get("/api/orders/order-123"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getOrderByNo_returnsOrder() throws Exception {
        when(orderService.findByOrderNo("ORDER-001")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);

        mockMvc.perform(get("/api/orders/no/ORDER-001"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createOrder_returnsCreatedOrder() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);

        OrderItem item = new OrderItem();
        Product product = new Product();
        product.setId("prod-1");
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(50));

        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.userId = "user-123";
        request.items = List.of(item);

        when(orderService.create(eq("user-123"), any())).thenReturn(testOrder);

        mockMvc.perform(post("/api/orders")
                .param("userId", "user-123")
                .contentType("application/json")
                .content("{\"userId\":\"user-123\",\"items\":[{\"product\":{\"id\":\"prod-1\"},\"quantity\":2}]}"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createOrder_throwsUnauthorized_whenNotCurrentUser() throws Exception {
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(post("/api/orders")
                .param("userId", "user-123")
                .contentType("application/json")
                .content("{\"userId\":\"user-123\",\"items\":[]}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateStatus_returnsUpdatedOrder() throws Exception {
        when(orderService.findById("order-123")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.updateStatus("order-123", Order.Status.SHIPPED)).thenReturn(testOrder);

        mockMvc.perform(patch("/api/orders/order-123/status")
                .param("status", "SHIPPED"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void payOrder_returnsPaidOrder() throws Exception {
        when(orderService.findById("order-123")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(true);
        when(orderService.pay("order-123", "TRADE-001")).thenReturn(testOrder);

        mockMvc.perform(patch("/api/orders/order-123/pay")
                .param("tradeNo", "TRADE-001"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void payOrder_throwsUnauthorized_whenNotOwner() throws Exception {
        when(orderService.findById("order-123")).thenReturn(testOrder);
        when(securityUtils.isCurrentUser("user-123")).thenReturn(false);

        mockMvc.perform(patch("/api/orders/order-123/pay")
                .param("tradeNo", "TRADE-001"))
            .andExpect(status().isForbidden());
    }
}