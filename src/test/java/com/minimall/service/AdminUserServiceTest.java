package com.minimall.service;

import com.minimall.model.AdminUser;
import com.minimall.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("findByUsername returns user when exists")
    void findByUsername_success() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");
        admin.setUsername("admin");

        when(adminUserRepository.findByUsernameAndActiveTrue("admin"))
            .thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    @DisplayName("findByUsername returns empty when not found")
    void findByUsername_notFound() {
        when(adminUserRepository.findByUsernameAndActiveTrue("nonexistent"))
            .thenReturn(Optional.empty());

        Optional<AdminUser> result = adminUserService.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("validatePassword returns true when password matches")
    void validatePassword_success() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("encoded-hash");

        when(passwordEncoder.matches("raw-password", "encoded-hash")).thenReturn(true);

        boolean result = adminUserService.validatePassword(admin, "raw-password");

        assertTrue(result);
    }

    @Test
    @DisplayName("validatePassword returns false when password doesn't match")
    void validatePassword_failure() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("encoded-hash");

        when(passwordEncoder.matches("wrong-password", "encoded-hash")).thenReturn(false);

        boolean result = adminUserService.validatePassword(admin, "wrong-password");

        assertFalse(result);
    }

    @Test
    @DisplayName("createAdmin throws when username exists")
    void createAdmin_usernameExists() {
        when(adminUserRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> adminUserService.createAdmin("existing", "pass", AdminUser.Role.ADMIN));
    }

    @Test
    @DisplayName("createAdmin creates new admin")
    void createAdmin_success() {
        when(adminUserRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(i -> {
            AdminUser admin = i.getArgument(0);
            admin.setId("new-id");
            return admin;
        });

        AdminUser result = adminUserService.createAdmin("newadmin", "password", AdminUser.Role.ADMIN);

        assertNotNull(result);
        assertEquals("newadmin", result.getUsername());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals(AdminUser.Role.ADMIN, result.getRole());
        assertTrue(result.getActive());
    }

    @Test
    @DisplayName("findById returns user by id")
    void findById_success() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");

        when(adminUserRepository.findById("admin-1")).thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findById("admin-1");

        assertTrue(result.isPresent());
        assertEquals("admin-1", result.get().getId());
    }
}