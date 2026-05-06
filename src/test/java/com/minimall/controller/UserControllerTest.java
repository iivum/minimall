package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.User;
import com.minimall.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private User createTestUser(String id) {
        User user = new User();
        user.setId(id);
        user.setOpenid("test-openid");
        user.setNickname("Test User");
        user.setPhone("1234567890");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        return user;
    }

    @Test
    @WithMockUser
    void getUser_returnsUser_whenAuthorized() throws Exception {
        String userId = "user-123";
        User user = createTestUser(userId);
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);
        when(userService.findById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.nickname").value("Test User"))
            .andExpect(jsonPath("$.phone").value("1234567890"))
            .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"));
    }

    @Test
    @WithMockUser
    void getUser_throwsForbidden_whenNotOwner() throws Exception {
        String userId = "user-123";
        when(securityUtils.isCurrentUser(userId)).thenReturn(false);

        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getUserByOpenid_returnsUser() throws Exception {
        String openid = "test-openid";
        User user = createTestUser("user-123");
        user.setOpenid(openid);
        when(userService.findByOpenid(openid)).thenReturn(user);

        mockMvc.perform(get("/api/users/openid/{openid}", openid))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("user-123"))
            .andExpect(jsonPath("$.openid").value(openid));
    }

    @Test
    @WithMockUser
    void updateUser_returnsUpdatedUser_whenAuthorized() throws Exception {
        String userId = "user-123";
        User user = createTestUser(userId);
        user.setNickname("Updated Name");
        when(securityUtils.isCurrentUser(userId)).thenReturn(true);
        when(userService.update(eq(userId), any())).thenReturn(user);

        String requestBody = """
            {
                "nickname": "Updated Name",
                "phone": "1234567890",
                "avatarUrl": "https://example.com/avatar.jpg"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("Updated Name"));
    }

    @Test
    @WithMockUser
    void updateUser_throwsForbidden_whenNotOwner() throws Exception {
        String userId = "user-123";
        when(securityUtils.isCurrentUser(userId)).thenReturn(false);

        String requestBody = """
            {
                "nickname": "Updated Name",
                "phone": "1234567890",
                "avatarUrl": "https://example.com/avatar.jpg"
            }
            """;

        mockMvc.perform(put("/api/users/{id}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void getUser_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/user-123"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        String requestBody = """
            {
                "nickname": "Updated Name",
                "phone": "1234567890",
                "avatarUrl": "https://example.com/avatar.jpg"
            }
            """;

        mockMvc.perform(put("/api/users/user-123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }
}
