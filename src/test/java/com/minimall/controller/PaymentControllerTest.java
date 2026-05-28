package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.JwtService;
import com.minimall.service.OrderService;
import com.minimall.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void initiatePayment_returnsPaymentInfo() throws Exception {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");

        when(orderService.findById("order-1")).thenReturn(order);
        when(paymentService.initiatePayment(order, BigDecimal.valueOf(99.99)))
            .thenReturn("PAY-12345");

        mockMvc.perform(post("/api/payments/initiate")
                .with(csrf())
                .param("orderId", "order-1")
                .param("amount", "99.99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value("PAY-12345"))
            .andExpect(jsonPath("$.orderId").value("order-1"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void getPaymentStatus_returnsStatus() throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("paymentId", "PAY-12345");
        status.put("status", "PENDING");
        status.put("message", "Processing");

        when(paymentService.getPaymentStatus("PAY-12345")).thenReturn(status);

        mockMvc.perform(get("/api/payments/status/PAY-12345"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value("PAY-12345"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void handleCallback_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/payments/callback")
                .with(csrf())
                .param("transactionId", "TXN-123")
                .param("status", "SUCCESS"))
            .andExpect(status().isOk())
            .andExpect(content().string("SUCCESS"));
    }
}