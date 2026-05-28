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
    @DisplayName("findByUsername returns admin when exists")
    void findByUsername_exists_returnsAdmin() {
        AdminUser admin = new AdminUser();
        admin.setId("admin-1");
        admin.setUsername("admin");

        when(adminUserRepository.findByUsernameAndActiveTrue("admin")).thenReturn(Optional.of(admin));

        Optional<AdminUser> result = adminUserService.findByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals("admin-1", result.get().getId());
    }

    @Test
    @DisplayName("validatePassword returns true for correct password")
    void validatePassword_correct_returnsTrue() {
        AdminUser admin = new AdminUser();
        admin.setPasswordHash("encoded-hash");

        when(passwordEncoder.matches("password123", "encoded-hash")).thenReturn(true);

        boolean result = adminUserService.validatePassword(admin, "password123");

        assertTrue(result);
    }

    @Test
    @DisplayName("createAdmin encodes password and saves")
    void createAdmin_encodesAndSaves() {
        when(adminUserRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(i -> {
            AdminUser admin = i.getArgument(0);
            admin.setId("new-id");
            return admin;
        });

        AdminUser result = adminUserService.createAdmin("newadmin", "password123", AdminUser.Role.ADMIN);

        assertNotNull(result);
        assertEquals("newadmin", result.getUsername());
        assertEquals("encoded-password", result.getPasswordHash());
    }

    @Test
    @DisplayName("createAdmin throws when username exists")
    void createAdmin_usernameExists_throws() {
        when(adminUserRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            adminUserService.createAdmin("existing", "password123", AdminUser.Role.ADMIN));
    }
}
