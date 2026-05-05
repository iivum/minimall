package com.minimall.controller;

import com.minimall.model.CustomerServiceMessage;
import com.minimall.service.CustomerServiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerServiceController.class)
class CustomerServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerServiceService customerService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    @Test
    @WithMockUser
    void getPendingMessages_returnsMessageList() throws Exception {
        when(customerService.getPendingMessages()).thenReturn(List.of());

        mockMvc.perform(get("/api/customer-service/pending"))
            .andExpect(status().isOk());
    }
}
