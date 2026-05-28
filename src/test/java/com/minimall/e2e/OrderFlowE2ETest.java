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

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
class OrderFlowE2ETest {

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
    void createOrderFlow_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content("{\"userId\":\"test-user-id\",\"items\":[]}"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void getOrderFlow_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/orders/ORD-001"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void listOrdersFlow_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/orders/user/test-user-id"))
                .andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void createOrderFlow_withAuth_butInvalidData_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + validToken)
                .contentType("application/json")
                .content("{\"userId\":\"test-user-id\",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @org.junit.jupiter.api.Test
    void getOrderFlow_withAuth_butOrderNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/orders/NON-EXISTENT")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }
}