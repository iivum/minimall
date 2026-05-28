package com.minimall.e2e;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证流程 E2E 测试
 *
 * <p>测试用户登录、令牌验证等认证相关功能。
 * 使用 E2ETestBase 提供的标准化认证流程。
 */
class AuthFlowE2ETest extends E2ETestBase {

    @Test
    void loginFlow_withValidCredentials_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"openid\":\"testuser\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void loginFlow_withInvalidCredentials_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"openid\":\"nonexistent\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void loginFlow_withValidCredentials_returnsToken() throws Exception {
        String token = loginAndGetToken();
        assert token != null && !token.isEmpty() : "Token should be returned";
    }

    @Test
    void authenticatedRequest_withValidToken_succeeds() throws Exception {
        String token = loginAndGetToken();

        authenticatedGet("/api/orders")
                .withToken(token)
                .execute(status().isOk());
    }
}