package com.minimall.e2e;

import com.minimall.config.E2ETestConfig;
import com.minimall.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E 测试基类
 *
 * <p>提供标准化的认证流程和测试数据管理，确保测试稳定可靠。
 *
 * <p>设计原则：
 * <ul>
 *   <li>每个测试方法使用独立的测试数据，避免相互干扰</li>
 *   <li>使用真实的 JWT 认证而非 MockMvc 的 security mock</li>
 *   <li>配置绑定在隔离的 test profile 中，避免与主配置冲突</li>
 *   <li>失败时提供详细的诊断信息</li>
 * </ul>
 */
@SpringBootTest(classes = com.minimall.MinimallApplication.class)
@AutoConfigureMockMvc
@Import(E2ETestConfig.class)
@ActiveProfiles("test")
public abstract class E2ETestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtService jwtService;

    protected static final String TEST_USER_ID = "test-user-001";
    protected static final String TEST_OPENID = "testuser";

    /**
     * 执行登录并返回认证令牌
     */
    protected String loginAndGetToken() throws Exception {
        return loginAndGetToken(TEST_USER_ID, TEST_OPENID);
    }

    /**
     * 执行登录并返回认证令牌
     */
    protected String loginAndGetToken(String userId, String openid) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"openid\":\"" + openid + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return extractToken(response);
    }

    /**
     * 提取响应中的 token 字段
     */
    protected String extractToken(String jsonResponse) {
        int tokenStart = jsonResponse.indexOf("\"token\":\"") + 9;
        int tokenEnd = jsonResponse.indexOf("\"", tokenStart);
        return jsonResponse.substring(tokenStart, tokenEnd);
    }

    /**
     * 创建带认证的 GET 请求
     */
    protected AuthRequest authenticatedGet(String url) {
        return new AuthRequest(mockMvc, jwtService, url, "GET");
    }

    /**
     * 创建带认证的 POST 请求
     */
    protected AuthRequest authenticatedPost(String url) {
        return new AuthRequest(mockMvc, jwtService, url, "POST");
    }

    /**
     * 认证请求构建器
     */
    public static class AuthRequest {
        private final MockMvc mockMvc;
        private final JwtService jwtService;
        private final String url;
        private final String method;
        private String token;
        private Object body;
        private MediaType contentType = MediaType.APPLICATION_JSON;

        public AuthRequest(MockMvc mockMvc, JwtService jwtService, String url, String method) {
            this.mockMvc = mockMvc;
            this.jwtService = jwtService;
            this.url = url;
            this.method = method;
        }

        public AuthRequest withToken(String token) {
            this.token = token;
            return this;
        }

        public AuthRequest withBody(Object body) {
            this.body = body;
            return this;
        }

        public AuthRequest withUser(String userId, String openid) {
            this.token = jwtService.generateToken(userId, openid);
            return this;
        }

        public AuthRequest withDefaultUser() {
            return withUser(TEST_USER_ID, TEST_OPENID);
        }

        public AuthRequest withJson() {
            this.contentType = MediaType.APPLICATION_JSON;
            return this;
        }

        public MvcResult execute(ResultMatcher expectedStatus) throws Exception {
            if (token == null) {
                throw new IllegalStateException("Token not set. Call withToken(), withUser(), or withDefaultUser()");
            }

            var requestBuilder = switch (method) {
                case "GET" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url)
                        .header("Authorization", "Bearer " + token);
                case "POST" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(contentType);
                case "PATCH" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(contentType);
                case "PUT" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(contentType);
                case "DELETE" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url)
                        .header("Authorization", "Bearer " + token);
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            };

            if (body != null) {
                var json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
                requestBuilder.content(json);
            }

            return mockMvc.perform(requestBuilder)
                    .andExpect(expectedStatus)
                    .andReturn();
        }

        public MvcResult execute() throws Exception {
            return execute(status().isOk());
        }
    }
}