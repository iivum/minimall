package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    @Test
    @WithMockUser
    void getAllOrders_returnsPaginatedOrders() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].orderNo").value("ORD-001"));
    }

    @Test
    @WithMockUser
    void getAllOrdersAll_returnsAllOrders() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderService.findAll()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/admin/orders/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderNo").value("ORD-001"));
    }

    @Test
    @WithMockUser
    void getOrder_returnsOrderById() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderService.findById("order-1")).thenReturn(order);

        mockMvc.perform(get("/api/admin/orders/order-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNo").value("ORD-001"));
    }

    @Test
    @WithMockUser
    void getOrderByNo_returnsOrderByOrderNo() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderService.findByOrderNo("ORD-001")).thenReturn(order);

        mockMvc.perform(get("/api/admin/orders/no/ORD-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderNo").value("ORD-001"));
    }

    @Test
    @WithMockUser
    void updateStatus_updatesOrderStatus() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setStatus(Order.Status.PAID);

        when(orderService.updateStatus(eq("order-1"), eq(Order.Status.SHIPPED))).thenReturn(order);

        mockMvc.perform(patch("/api/admin/orders/order-1/status")
                .with(csrf())
                .param("status", "SHIPPED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser
    void getAllOrders_withDefaultPagination() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");

        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}