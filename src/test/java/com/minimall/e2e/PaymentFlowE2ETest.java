package com.minimall.e2e;

import com.minimall.MinimallApplication;
import com.minimall.config.TestMetricsConfig;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(TestMetricsConfig.class)
class PaymentFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RSAAutoCertificateConfig rsaAutoCertificateConfig;

    @Test
    void initiatePaymentFlow_withValidOrder_returnsPaymentInfo() throws Exception {
        mockMvc.perform(post("/api/payments/initiate")
                .param("orderId", "ORD-001")
                .param("amount", "99.99"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentStatusFlow_whenPaymentExists_returnsStatus() throws Exception {
        mockMvc.perform(get("/api/payments/status/PAY-001"))
                .andExpect(status().isOk());
    }

    @Test
    void callbackPaymentFlow_withValidSignature_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/payments/callback")
                .param("transactionId", "TXN-12345")
                .param("status", "SUCCESS"))
                .andExpect(status().isOk());
    }
}