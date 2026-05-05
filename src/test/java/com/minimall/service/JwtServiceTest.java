package com.minimall.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {
    private JwtService jwtService;
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-characters-long!!";
    private static final long EXPIRATION_MS = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_createsValidToken() {
        String token = jwtService.generateToken("user123", "openid_abc");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUserIdFromToken_extractsCorrectUserId() {
        String userId = "user123";
        String token = jwtService.generateToken(userId, "openid_abc");

        String extractedUserId = jwtService.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void getOpenidFromToken_extractsCorrectOpenid() {
        String openid = "openid_abc";
        String token = jwtService.generateToken("user123", openid);

        String extractedOpenid = jwtService.getOpenidFromToken(token);

        assertEquals(openid, extractedOpenid);
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtService.generateToken("user123", "openid_abc");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_returnsFalseForInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_returnsFalseForNullToken() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void validateToken_returnsFalseForEmptyToken() {
        assertFalse(jwtService.validateToken(""));
    }
}
