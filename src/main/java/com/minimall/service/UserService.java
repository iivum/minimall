package com.minimall.service;

import com.minimall.model.User;
import com.minimall.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        return userRepository.save(user);
    }

    public User update(String id, User updated) {
        User existing = findById(id);
        existing.setNickname(updated.getNickname());
        existing.setAvatarUrl(updated.getAvatarUrl());
        existing.setPhone(updated.getPhone());
        return userRepository.save(existing);
    }
}
