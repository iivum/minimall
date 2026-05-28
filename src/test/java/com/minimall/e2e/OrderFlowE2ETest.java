package com.minimall.e2e;

import com.minimall.MinimallApplication;
import com.minimall.config.E2ETestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
class OrderFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrderFlow_withValidData_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/orders")
                .param("productId", "PROD-001")
                .param("quantity", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderFlow_whenOrderExists_returnsOrder() throws Exception {
        mockMvc.perform(get("/api/orders/ORD-001"))
                .andExpect(status().isOk());
    }

    @Test
    void listOrdersFlow_returnsOrderList() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }
}