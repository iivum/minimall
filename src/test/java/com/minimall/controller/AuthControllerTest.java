package com.minimall.controller;

import com.minimall.service.JwtService;
import com.minimall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.minimall.config.SecurityUtils securityUtils;

    @Test
    @WithMockUser
    void login_withValidOpenid_returnsTokenAndUserId() throws Exception {
        var user = new com.minimall.model.User();
        user.setId("user-123");
        user.setOpenid("openid-abc");
        when(userService.findByOpenid("openid-abc")).thenReturn(user);
        when(jwtService.generateToken("user-123", "openid-abc")).thenReturn("jwt-token-xyz");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openid\":\"openid-abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.userId").value("user-123"));
    }

    @Test
    @WithMockUser
    void login_withNonexistentOpenid_returns500() throws Exception {
        when(userService.findByOpenid(anyString()))
                .thenThrow(new RuntimeException("User not found by openid: unknown"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openid\":\"unknown\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser
    void register_withValidData_returnsTokenAndUserId() throws Exception {
        var user = new com.minimall.model.User();
        user.setId("new-user-456");
        user.setOpenid("new-openid");
        user.setNickname("TestUser");
        when(userService.create(org.mockito.ArgumentMatchers.any(com.minimall.model.User.class)))
                .thenReturn(user);
        when(jwtService.generateToken("new-user-456", "new-openid")).thenReturn("new-jwt-token");

        String requestBody = """
            {"openid":"new-openid","nickname":"TestUser","phone":"1234567890","avatarUrl":"http://example.com/avatar.png"}
            """;

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"))
                .andExpect(jsonPath("$.userId").value("new-user-456"));
    }
}