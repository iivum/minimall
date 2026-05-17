package com.minimall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.model.User;
import com.minimall.service.JwtService;
import com.minimall.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("returns token when openid exists")
        void login_withValidOpenid_returnsToken() throws Exception {
            User user = new User();
            user.setId("user-123");
            user.setOpenid("openid_abc");
            when(userService.findByOpenid("openid_abc")).thenReturn(user);
            when(jwtService.generateToken("user-123", "openid_abc")).thenReturn("jwt-token-xyz");

            String requestBody = objectMapper.writeValueAsString(new AuthController.LoginRequest("openid_abc"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.userId").value("user-123"));
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void login_withNonexistentOpenid_throwsException() throws Exception {
            when(userService.findByOpenid("unknown_openid"))
                .thenThrow(new RuntimeException("User not found by openid: unknown_openid"));

            String requestBody = objectMapper.writeValueAsString(new AuthController.LoginRequest("unknown_openid"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns token with correct userId format")
        void login_withUUIDUserId_returnsCorrectFormat() throws Exception {
            User user = new User();
            user.setId("550e8400-e29b-41d4-a716-446655440000");
            user.setOpenid("openid_uuid");
            when(userService.findByOpenid("openid_uuid")).thenReturn(user);
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("token-abc");

            String requestBody = objectMapper.writeValueAsString(new AuthController.LoginRequest("openid_uuid"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("550e8400-e29b-41d4-a716-446655440000"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("creates user and returns token on success")
        void register_withValidData_returnsToken() throws Exception {
            User createdUser = new User();
            createdUser.setId("new-user-456");
            createdUser.setOpenid("new_openid");
            createdUser.setNickname("TestUser");
            createdUser.setPhone("1234567890");
            createdUser.setAvatarUrl("https://example.com/avatar.jpg");
            when(userService.create(any(User.class))).thenReturn(createdUser);
            when(jwtService.generateToken("new-user-456", "new_openid")).thenReturn("new-token-123");

            String requestBody = objectMapper.writeValueAsString(
                new AuthController.RegisterRequest("new_openid", "TestUser", "1234567890", "https://example.com/avatar.jpg")
            );

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token-123"))
                .andExpect(jsonPath("$.userId").value("new-user-456"));
        }

        @Test
        @DisplayName("returns 500 when user creation fails")
        void register_whenServiceThrows_returns500() throws Exception {
            when(userService.create(any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

            String requestBody = objectMapper.writeValueAsString(
                new AuthController.RegisterRequest("fail_openid", "FailUser", "0000000000", null)
            );

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("registers user with null avatarUrl")
        void register_withNullAvatar_returnsToken() throws Exception {
            User createdUser = new User();
            createdUser.setId("user-no-avatar");
            createdUser.setOpenid("openid_no_avatar");
            createdUser.setNickname("NoAvatar");
            createdUser.setPhone("1111111111");
            createdUser.setAvatarUrl(null);
            when(userService.create(any(User.class))).thenReturn(createdUser);
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("token-no-avatar");

            String requestBody = objectMapper.writeValueAsString(
                new AuthController.RegisterRequest("openid_no_avatar", "NoAvatar", "1111111111", null)
            );

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-no-avatar"));
        }
    }

    @Nested
    @DisplayName("JWT Token Generation")
    class JwtTokenTests {

        @Test
        @DisplayName("generates token with correct userId and openid")
        void jwtService_generateToken_calledWithCorrectParams() throws Exception {
            User user = new User();
            user.setId("jwt-user-789");
            user.setOpenid("jwt_openid");
            when(userService.findByOpenid("jwt_openid")).thenReturn(user);
            when(jwtService.generateToken("jwt-user-789", "jwt_openid")).thenReturn("correct-token");

            String requestBody = objectMapper.writeValueAsString(new AuthController.LoginRequest("jwt_openid"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("correct-token"));
        }
    }
}