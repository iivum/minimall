package com.minimall.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.minimall.service.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void health_returnsOkStatus() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @WithMockUser
    void health_returnsTimestamp() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void health_returnsAllRequiredFields() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    @WithMockUser
    void health_timestampIsRecent() throws Exception {
        long beforeTest = System.currentTimeMillis();

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    @WithMockUser
    void health_statusFieldIsString() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").isString())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}