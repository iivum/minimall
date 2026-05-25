package com.minimall.e2e;

import com.minimall.MinimallApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
class RefundFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requestRefundFlow_withValidOrder_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/refunds")
                .param("orderId", "ORD-001")
                .param("reason", "Product damaged"))
                .andExpect(status().isOk());
    }

    @Test
    void getRefundStatusFlow_whenRefundExists_returnsStatus() throws Exception {
        mockMvc.perform(get("/api/refunds/status/REF-001"))
                .andExpect(status().isOk());
    }

    @Test
    void listRefundsFlow_returnsRefundList() throws Exception {
        mockMvc.perform(get("/api/refunds"))
                .andExpect(status().isOk());
    }
}