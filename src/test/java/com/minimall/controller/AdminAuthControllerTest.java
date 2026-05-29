package com.minimall.controller;

import com.minimall.model.AdminUser;
import com.minimall.service.AdminUserService;
import com.minimall.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuthController.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void login_withValidCredentials_returnsTokenAndUserInfo() throws Exception {
        AdminUser admin = new AdminUser();
        admin.setId("admin-123");
        admin.setUsername("admin");
        admin.setPasswordHash("$2a$10$hashedpassword");
        admin.setRole(com.minimall.model.AdminUser.Role.ADMIN);

        when(adminUserService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(adminUserService.validatePassword(admin, "password123")).thenReturn(true);
        when(jwtService.generateAdminToken(anyString(), anyString(), anyString())).thenReturn("jwt-token-xyz");

        String requestBody = """
            {
                "username": "admin",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/admin/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
            .andExpect(jsonPath("$.adminId").value("admin-123"))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser
    void login_withInvalidPassword_returnsBadRequest() throws Exception {
        AdminUser admin = new AdminUser();
        admin.setId("admin-123");
        admin.setUsername("admin");
        admin.setPasswordHash("$2a$10$hashedpassword");
        admin.setRole(com.minimall.model.AdminUser.Role.ADMIN);

        when(adminUserService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(adminUserService.validatePassword(admin, "wrongpassword")).thenReturn(false);

        String requestBody = """
            {
                "username": "admin",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/admin/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void login_withNonexistentUser_returnsBadRequest() throws Exception {
        when(adminUserService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        String requestBody = """
            {
                "username": "nonexistent",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/admin/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void logout_returnsOk() throws Exception {
        mockMvc.perform(post("/api/admin/auth/logout")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void login_withOperatorRole_returnsCorrectRole() throws Exception {
        AdminUser admin = new AdminUser();
        admin.setId("operator-456");
        admin.setUsername("operator");
        admin.setPasswordHash("$2a$10$hashedpassword");
        admin.setRole(com.minimall.model.AdminUser.Role.SUPER_ADMIN);

        when(adminUserService.findByUsername("operator")).thenReturn(Optional.of(admin));
        when(adminUserService.validatePassword(admin, "password123")).thenReturn(true);
        when(jwtService.generateAdminToken(anyString(), anyString(), anyString())).thenReturn("operator-token");

        String requestBody = """
            {
                "username": "operator",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/admin/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));
    }
}