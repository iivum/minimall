package com.minimall.dto;

import com.minimall.model.User;

public record UserResponseDTO(
    String id,
    String openid,
    String nickname,
    String avatarUrl,
    String phone,
    String role
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getOpenid(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getPhone(),
            user.getRole().name()
        );
    }
}