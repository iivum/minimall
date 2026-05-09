package com.minimall.controller;

import com.minimall.model.AdminUser;
import com.minimall.service.AdminUserService;
import com.minimall.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "AdminAuth", description = "Admin Authentication APIs")
public class AdminAuthController {
    private final AdminUserService adminUserService;
    private final JwtService jwtService;

    public AdminAuthController(AdminUserService adminUserService, JwtService jwtService) {
        this.adminUserService = adminUserService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Admin login with username and password")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        AdminUser admin = adminUserService.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!adminUserService.validatePassword(admin, request.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole().name());
        return ResponseEntity.ok(new LoginResponse(token, admin.getId(), admin.getUsername(), admin.getRole().name()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Admin logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String adminId, String username, String role) {}
}