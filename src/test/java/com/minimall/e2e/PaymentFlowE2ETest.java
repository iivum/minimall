package com.minimall.e2e;

import com.minimall.MinimallApplication;
import com.minimall.config.E2ETestConfig;
import com.minimall.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
class PaymentFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = jwtService.generateToken("test-user-id", "testuser");
    }

    @org.junit.jupiter.api.Test
    void initiatePaymentFlow_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/api/payments/initiate")
                .param("orderId", "ORD-001")
                .param("amount", "99.99"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void getPaymentStatusFlow_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/payments/status/PAY-001"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void callbackPaymentFlow_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/api/payments/callback")
                .param("transactionId", "TXN-12345")
                .param("status", "SUCCESS"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void initiatePaymentFlow_withAuth_butOrderNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/payments/initiate")
                .header("Authorization", "Bearer " + validToken)
                .param("orderId", "NON-EXISTENT")
                .param("amount", "99.99"))
                .andExpect(status().isNotFound());
    }

    @org.junit.jupiter.api.Test
    void getPaymentStatusFlow_withAuth_returnsStatus() throws Exception {
        mockMvc.perform(get("/api/payments/status/ANY-ID")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("ANY-ID"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}