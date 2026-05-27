package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.UserDTO;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.User;
import com.minimall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "User management APIs")
public class UserController {
    private final UserService userService;
    private final SecurityUtils securityUtils;

    public UserController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        if (!securityUtils.isCurrentUser(id)) {
            throw new UnauthorizedException("You can only access your own profile");
        }
        return ResponseEntity.ok(UserDTO.from(userService.findById(id)));
    }

    @GetMapping("/openid/{openid}")
    @Operation(summary = "Get user by WeChat openid")
    public ResponseEntity<UserDTO> getUserByOpenid(@PathVariable String openid) {
        return ResponseEntity.ok(UserDTO.from(userService.findByOpenid(openid)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user info")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id, @RequestBody User user) {
        if (!securityUtils.isCurrentUser(id)) {
            throw new UnauthorizedException("You can only update your own profile");
        }
        return ResponseEntity.ok(UserDTO.from(userService.update(id, user)));
    }
}