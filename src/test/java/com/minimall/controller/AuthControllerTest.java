package com.minimall.controller;

import com.minimall.model.User;
import com.minimall.service.JwtService;
import com.minimall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void login_withValidOpenid_returnsTokenAndUserId() throws Exception {
        User user = new User();
        user.setId("user-123");
        user.setOpenid("openid-abc");
        user.setNickname("TestUser");

        when(userService.findByOpenid("openid-abc")).thenReturn(user);
        when(jwtService.generateToken("user-123", "openid-abc")).thenReturn("jwt-token-xyz");

        String requestBody = """
            {
                "openid": "openid-abc"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
            .andExpect(jsonPath("$.userId").value("user-123"));
    }

    @Test
    @WithMockUser
    void register_withValidRequest_returnsTokenAndUserId() throws Exception {
        User createdUser = new User();
        createdUser.setId("new-user-456");
        createdUser.setOpenid("new-openid");
        createdUser.setNickname("NewUser");
        createdUser.setPhone("13800138000");
        createdUser.setAvatarUrl("https://example.com/avatar.png");

        when(userService.create(any(User.class))).thenReturn(createdUser);
        when(jwtService.generateToken("new-user-456", "new-openid")).thenReturn("new-jwt-token");

        String requestBody = """
            {
                "openid": "new-openid",
                "nickname": "NewUser",
                "phone": "13800138000",
                "avatarUrl": "https://example.com/avatar.png"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("new-jwt-token"))
            .andExpect(jsonPath("$.userId").value("new-user-456"));
    }

    @Test
    @WithMockUser
    void login_withEmptyOpenid_returnsOk() throws Exception {
        User user = new User();
        user.setId("user-empty");
        user.setOpenid("");
        user.setNickname("EmptyUser");

        when(userService.findByOpenid("")).thenReturn(user);
        when(jwtService.generateToken("user-empty", "")).thenReturn("empty-token");

        String requestBody = """
            {
                "openid": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("empty-token"))
            .andExpect(jsonPath("$.userId").value("user-empty"));
    }

    @Test
    @WithMockUser
    void register_withEmptyNickname_returnsOk() throws Exception {
        User user = new User();
        user.setId("user-empty-nick");
        user.setOpenid("openid-empty-nick");
        user.setNickname("");
        user.setPhone("13800138000");
        user.setAvatarUrl("https://example.com/avatar.png");

        when(userService.create(any(User.class))).thenReturn(user);
        when(jwtService.generateToken("user-empty-nick", "openid-empty-nick")).thenReturn("empty-nick-token");

        String requestBody = """
            {
                "openid": "openid-empty-nick",
                "nickname": "",
                "phone": "13800138000",
                "avatarUrl": "https://example.com/avatar.png"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("empty-nick-token"))
            .andExpect(jsonPath("$.userId").value("user-empty-nick"));
    }

    @Test
    @WithMockUser
    void login_withNonExistentOpenid_returnsOk() throws Exception {
        when(userService.findByOpenid("non-existent-openid")).thenReturn(null);

        String requestBody = """
            {
                "openid": "non-existent-openid"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void register_createsUserWithAllFields() throws Exception {
        User user = new User();
        user.setOpenid("full-openid");
        user.setNickname("Full User");
        user.setPhone("13900139000");
        user.setAvatarUrl("https://example.com/full.png");
        user.setId("full-user-789");

        when(userService.create(any(User.class))).thenReturn(user);
        when(jwtService.generateToken("full-user-789", "full-openid")).thenReturn("full-token");

        String requestBody = """
            {
                "openid": "full-openid",
                "nickname": "Full User",
                "phone": "13900139000",
                "avatarUrl": "https://example.com/full.png"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("full-token"))
            .andExpect(jsonPath("$.userId").value("full-user-789"));
    }
}