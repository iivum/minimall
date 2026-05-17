package com.minimall.controller;

import com.minimall.config.JwtAuthenticationFilter;
import com.minimall.model.User;
import com.minimall.service.JwtService;
import com.minimall.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("login with valid openid returns token")
    @WithMockUser
    void login_validOpenid_returnsToken() throws Exception {
        User user = new User();
        user.setId("user-123");
        user.setOpenid("test-openid");
        user.setNickname("TestUser");

        when(userService.findByOpenid("test-openid")).thenReturn(user);
        when(jwtService.generateToken("user-123", "test-openid")).thenReturn("jwt-token-xyz");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openid\":\"test-openid\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.userId").value("user-123"));
    }

    @Test
    @DisplayName("register with valid request creates user and returns token")
    @WithMockUser
    void register_validRequest_createsUserAndReturnsToken() throws Exception {
        User createdUser = new User();
        createdUser.setId("new-user-456");
        createdUser.setOpenid("new-openid");
        createdUser.setNickname("NewUser");
        createdUser.setPhone("1234567890");
        createdUser.setAvatarUrl("http://example.com/avatar.png");

        when(userService.create(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(createdUser);
        when(jwtService.generateToken("new-user-456", "new-openid")).thenReturn("new-jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openid\":\"new-openid\",\"nickname\":\"NewUser\",\"phone\":\"1234567890\",\"avatarUrl\":\"http://example.com/avatar.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"))
                .andExpect(jsonPath("$.userId").value("new-user-456"));
    }
}