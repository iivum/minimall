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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
class AuthFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginFlow_withValidCredentials_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .param("username", "testuser")
                .param("password", "password123"))
                .andExpect(status().isOk());
    }

    @Test
    void loginFlow_withInvalidCredentials_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .param("username", "testuser")
                .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized());
    }
}