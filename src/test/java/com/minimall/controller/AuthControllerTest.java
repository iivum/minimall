package com.minimall.controller;

import com.minimall.config.SecurityConfig;
import com.minimall.model.User;
import com.minimall.service.JwtService;
import com.minimall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    private User createTestUser(String id, String openid) {
        User user = new User();
        user.setId(id);
        user.setOpenid(openid);
        user.setNickname("Test User");
        user.setPhone("1234567890");
        return user;
    }

    @Test
    void login_returnsToken_whenUserExists() throws Exception {
        String openid = "test-openid";
        String userId = "user-123";
        String token = "jwt-token-abc123";
        User user = createTestUser(userId, openid);

        when(userService.findByOpenid(openid)).thenReturn(user);
        when(jwtService.generateToken(userId, openid)).thenReturn(token);

        String requestBody = """
            {
                "openid": "test-openid"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(token))
            .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void login_returnsError_whenUserNotFound() throws Exception {
        String openid = "nonexistent-openid";
        when(userService.findByOpenid(openid)).thenReturn(null);

        String requestBody = """
            {
                "openid": "nonexistent-openid"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().is5xxServerError());
    }

    @Test
    void register_returnsToken_whenSuccessful() throws Exception {
        String openid = "new-openid";
        String userId = "new-user-123";
        String token = "jwt-token-new";
        User user = createTestUser(userId, openid);

        when(userService.create(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(userId, openid)).thenReturn(token);

        String requestBody = """
            {
                "openid": "new-openid",
                "nickname": "New User",
                "phone": "9876543210",
                "avatarUrl": "https://example.com/avatar.jpg"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(token))
            .andExpect(jsonPath("$.userId").value(userId));
    }
}