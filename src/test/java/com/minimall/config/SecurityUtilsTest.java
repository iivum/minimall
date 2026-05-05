package com.minimall.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUtilsTest {
    private SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_returnsNullWhenNoAuthentication() {
        assertNull(securityUtils.getCurrentUserId());
    }

    @Test
    void isCurrentUser_returnsFalseWhenNoAuthentication() {
        assertFalse(securityUtils.isCurrentUser("user123"));
    }

    @Test
    void getCurrentUserId_returnsUserIdFromPrincipal() {
        JwtAuthenticationFilter.UserPrincipal principal =
                new JwtAuthenticationFilter.UserPrincipal("user123", "openid_abc");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertEquals("user123", securityUtils.getCurrentUserId());
    }

    @Test
    void isCurrentUser_returnsTrueWhenMatchingUserId() {
        JwtAuthenticationFilter.UserPrincipal principal =
                new JwtAuthenticationFilter.UserPrincipal("user123", "openid_abc");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertTrue(securityUtils.isCurrentUser("user123"));
    }

    @Test
    void isCurrentUser_returnsFalseWhenDifferentUserId() {
        JwtAuthenticationFilter.UserPrincipal principal =
                new JwtAuthenticationFilter.UserPrincipal("user123", "openid_abc");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertFalse(securityUtils.isCurrentUser("differentUser"));
    }
}
