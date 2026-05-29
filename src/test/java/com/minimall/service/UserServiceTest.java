package com.minimall.service;

import com.minimall.model.User;
import com.minimall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void findById_returnsUser_whenExists() {
        User user = new User();
        user.setId("user-1");
        user.setNickname("TestUser");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        User result = userService.findById("user-1");

        assertNotNull(result);
        assertEquals("TestUser", result.getNickname());
        verify(userRepository).findById("user-1");
    }

    @Test
    void findById_throwsException_whenNotFound() {
        when(userRepository.findById("not-found")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.findById("not-found"));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void findByOpenid_returnsUser_whenExists() {
        User user = new User();
        user.setId("user-1");
        user.setOpenid("openid-123");
        when(userRepository.findByOpenid("openid-123")).thenReturn(Optional.of(user));

        User result = userService.findByOpenid("openid-123");

        assertNotNull(result);
        assertEquals("openid-123", result.getOpenid());
        verify(userRepository).findByOpenid("openid-123");
    }

    @Test
    void findByOpenid_throwsException_whenNotFound() {
        when(userRepository.findByOpenid("unknown-openid")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.findByOpenid("unknown-openid"));

        assertTrue(exception.getMessage().contains("User not found by openid"));
    }

    @Test
    void create_savesAndReturnsUser() {
        User user = new User();
        user.setNickname("NewUser");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.create(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void save_savesAndReturnsUser() {
        User user = new User();
        user.setNickname("SaveUser");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.save(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void update_modifiesUserFields() {
        User existing = new User();
        existing.setId("user-1");
        existing.setNickname("OldNickname");
        existing.setPhone("123456");

        User updated = new User();
        updated.setNickname("NewNickname");
        updated.setAvatarUrl("http://example.com/avatar.jpg");
        updated.setPhone("654321");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.update("user-1", updated);

        assertEquals("NewNickname", existing.getNickname());
        assertEquals("http://example.com/avatar.jpg", existing.getAvatarUrl());
        assertEquals("654321", existing.getPhone());
        verify(userRepository).save(existing);
    }

    @Test
    void update_throwsException_whenUserNotFound() {
        when(userRepository.findById("not-found")).thenReturn(Optional.empty());

        User updated = new User();
        updated.setNickname("NewNickname");

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.update("not-found", updated));

        assertTrue(exception.getMessage().contains("User not found"));
    }
}