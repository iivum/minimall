package com.minimall.service;

import com.minimall.model.AdminUser;
import com.minimall.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(adminUserRepository, passwordEncoder);
    }

    @Test
    void findByUsername_returnsUserWhenExists() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");
        admin.setUsername("testadmin");
        when(adminUserRepository.findByUsernameAndActiveTrue("testadmin"))
            .thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findByUsername("testadmin");

        assertTrue(result.isPresent());
        assertEquals("testadmin", result.get().getUsername());
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        when(adminUserRepository.findByUsernameAndActiveTrue("unknown"))
            .thenReturn(Optional.empty());

        Optional<AdminUser> result = adminUserService.findByUsername("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void validatePassword_returnsTrueWhenMatch() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("hashedPassword");
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

        boolean result = adminUserService.validatePassword(admin, "rawPassword");

        assertTrue(result);
    }

    @Test
    void validatePassword_returnsFalseWhenNoMatch() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("hashedPassword");
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        boolean result = adminUserService.validatePassword(admin, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void createAdmin_encodesPasswordAndSaves() {
        when(adminUserRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(invocation -> {
            AdminUser saved = invocation.getArgument(0);
            saved.setId("new-admin-id");
            return saved;
        });

        AdminUser result = adminUserService.createAdmin("newadmin", "password123", AdminUser.Role.ADMIN);

        assertNotNull(result);
        assertEquals("newadmin", result.getUsername());
        assertEquals("encodedPassword", result.getPasswordHash());
        assertEquals(AdminUser.Role.ADMIN, result.getRole());
        assertTrue(result.getActive());
    }

    @Test
    void createAdmin_throwsWhenUsernameExists() {
        when(adminUserRepository.existsByUsername("existingadmin")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            adminUserService.createAdmin("existingadmin", "password123", AdminUser.Role.ADMIN));
    }

    @Test
    void findById_returnsUserWhenExists() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");
        when(adminUserRepository.findById("admin-1")).thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findById("admin-1");

        assertTrue(result.isPresent());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(adminUserRepository.findById("unknown")).thenReturn(Optional.empty());

        Optional<AdminUser> result = adminUserService.findById("unknown");

        assertTrue(result.isEmpty());
    }
}