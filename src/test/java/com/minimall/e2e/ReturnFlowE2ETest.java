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
class ReturnFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requestReturnFlow_withValidOrder_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/returns")
                .param("orderId", "ORD-001")
                .param("reason", "Wrong size"))
                .andExpect(status().isOk());
    }

    @Test
    void getReturnStatusFlow_whenReturnExists_returnsStatus() throws Exception {
        mockMvc.perform(get("/api/returns/status/RET-001"))
                .andExpect(status().isOk());
    }

    @Test
    void listReturnsFlow_returnsReturnList() throws Exception {
        mockMvc.perform(get("/api/returns"))
                .andExpect(status().isOk());
    }
}