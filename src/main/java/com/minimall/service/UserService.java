package com.minimall.service;

import com.minimall.model.User;
import com.minimall.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    public UserService(UserRepository userRepository, AnalyticsService analyticsService) {
        this.userRepository = userRepository;
        this.analyticsService = analyticsService;
    }

    public User findById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User findByOpenid(String openid) {
        return userRepository.findByOpenid(openid)
            .orElseThrow(() -> new RuntimeException("User not found by openid: " + openid));
    }

    public User create(User user) {
        User saved = userRepository.save(user);
        analyticsService.track("USER_REGISTER", saved.getId(), "USER", saved.getId(), null);
        return saved;
    }

    public User update(String id, User updated) {
        User existing = findById(id);
        existing.setNickname(updated.getNickname());
        existing.setAvatarUrl(updated.getAvatarUrl());
        existing.setPhone(updated.getPhone());
        User saved = userRepository.save(existing);
        analyticsService.track("USER_UPDATE", id, "USER", id, null);
        return saved;
    }
}
