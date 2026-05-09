package com.minimall.service;

import com.minimall.model.AdminUser;
import com.minimall.repository.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminUserService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AdminUser> findByUsername(String username) {
        return adminUserRepository.findByUsernameAndActiveTrue(username);
    }

    public boolean validatePassword(AdminUser adminUser, String rawPassword) {
        return passwordEncoder.matches(rawPassword, adminUser.getPasswordHash());
    }

    public AdminUser createAdmin(String username, String password, AdminUser.Role role) {
        if (adminUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setActive(true);
        return adminUserRepository.save(admin);
    }

    public Optional<AdminUser> findById(String id) {
        return adminUserRepository.findById(id);
    }
}