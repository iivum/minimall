package com.minimall.controller;

import com.minimall.model.User;
import com.minimall.service.JwtService;
import com.minimall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with WeChat openid, returns JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findByOpenid(request.openid());
        String token = jwtService.generateToken(user.getId(), user.getOpenid());
        return ResponseEntity.ok(new LoginResponse(token, user.getId()));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setOpenid(request.openid());
        user.setNickname(request.nickname());
        user.setPhone(request.phone());
        user.setAvatarUrl(request.avatarUrl());
        User created = userService.create(user);
        String token = jwtService.generateToken(created.getId(), created.getOpenid());
        return ResponseEntity.ok(new LoginResponse(token, created.getId()));
    }

    public record LoginRequest(String openid) {}
    public record RegisterRequest(String openid, String nickname, String phone, String avatarUrl) {}
    public record LoginResponse(String token, String userId) {}
}
