package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.UserResponseDTO;
import com.minimall.exception.UnauthorizedException;
import com.minimall.model.User;
import com.minimall.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtils securityUtils;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, securityUtils);
    }

    @Test
    void getUser_returnsUser_whenCurrentUser() {
        User user = new User();
        user.setId("user-1");
        user.setNickname("Test User");

        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(userService.findById("user-1")).thenReturn(user);

        ResponseEntity<UserResponseDTO> response = controller.getUser("user-1");

        assertNotNull(response.getBody());
        assertEquals("user-1", response.getBody().id());
    }

    @Test
    void getUser_throwsUnauthorized_whenNotCurrentUser() {
        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> controller.getUser("other-user"));
    }

    @Test
    void getUserByOpenid_returnsUser() {
        User user = new User();
        user.setId("user-1");
        user.setOpenid("openid-123");

        when(userService.findByOpenid("openid-123")).thenReturn(user);

        ResponseEntity<UserResponseDTO> response = controller.getUserByOpenid("openid-123");

        assertNotNull(response.getBody());
        assertEquals("openid-123", response.getBody().openid());
    }

    @Test
    void updateUser_returnsUpdatedUser_whenCurrentUser() {
        User user = new User();
        user.setId("user-1");
        user.setNickname("Updated Name");

        when(securityUtils.isCurrentUser("user-1")).thenReturn(true);
        when(userService.update("user-1", user)).thenReturn(user);

        ResponseEntity<UserResponseDTO> response = controller.updateUser("user-1", user);

        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().nickname());
    }

    @Test
    void updateUser_throwsUnauthorized_whenNotCurrentUser() {
        User user = new User();
        user.setId("other-user");

        when(securityUtils.isCurrentUser("other-user")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> controller.updateUser("other-user", user));
    }
}
