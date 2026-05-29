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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private AdminUserRepository adminUserRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(adminUserRepository, passwordEncoder);
    }

    @Test
    void findByUsername_returnsUserWhenFound() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");
        admin.setUsername("admin");

        when(adminUserRepository.findByUsernameAndActiveTrue("admin")).thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        when(adminUserRepository.findByUsernameAndActiveTrue("nonexistent")).thenReturn(Optional.empty());

        Optional<AdminUser> result = adminUserService.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void validatePassword_returnsTrueWhenMatches() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("hashedPassword");

        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

        boolean result = adminUserService.validatePassword(admin, "rawPassword");

        assertTrue(result);
    }

    @Test
    void validatePassword_returnsFalseWhenDoesNotMatch() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("hashedPassword");

        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        boolean result = adminUserService.validatePassword(admin, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void createAdmin_success() {
        when(adminUserRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(inv -> {
            AdminUser admin = inv.getArgument(0);
            admin.setId("admin-new");
            return admin;
        });

        AdminUser result = adminUserService.createAdmin("newadmin", "password123", AdminUser.Role.ADMIN);

        assertNotNull(result);
        assertEquals("admin-new", result.getId());
        assertEquals("encodedPassword", result.getPasswordHash());
        assertEquals(AdminUser.Role.ADMIN, result.getRole());
        assertTrue(result.getActive());
    }

    @Test
    void createAdmin_throwsWhenUsernameExists() {
        when(adminUserRepository.existsByUsername("existingadmin")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            adminUserService.createAdmin("existingadmin", "password", AdminUser.Role.ADMIN)
        );
    }

    @Test
    void findById_returnsUserWhenFound() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");

        when(adminUserRepository.findById("admin-1")).thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findById("admin-1");

        assertTrue(result.isPresent());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(adminUserRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<AdminUser> result = adminUserService.findById("nonexistent");

        assertFalse(result.isPresent());
    }
}