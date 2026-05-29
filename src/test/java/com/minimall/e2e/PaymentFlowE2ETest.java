package com.minimall.e2e;

import com.minimall.MinimallApplication;
import com.minimall.config.E2ETestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
class PaymentFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void initiatePaymentFlow_withValidOrder_returnsPaymentInfo() throws Exception {
        mockMvc.perform(post("/api/payments/initiate")
                .with(user("testuser"))
                .param("orderId", "ORD-001")
                .param("amount", "99.99"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPaymentStatusFlow_whenPaymentExists_returnsStatus() throws Exception {
        mockMvc.perform(get("/api/payments/status/PAY-001")
                .with(user("testuser")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void callbackPaymentFlow_withValidSignature_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/payments/callback")
                .with(user("testuser"))
                .param("transactionId", "TXN-12345")
                .param("status", "SUCCESS"))
                .andExpect(status().isOk());
    }
}
