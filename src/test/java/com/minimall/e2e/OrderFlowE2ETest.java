package com.minimall.e2e;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 订单流程 E2E 测试
 *
 * <p>测试订单创建、查询等订单相关功能。
 * 使用 E2ETestBase 提供的标准化认证流程。
 */
class OrderFlowE2ETest extends E2ETestBase {

    @Test
    void createOrderFlow_withValidData_returnsSuccess() throws Exception {
        String token = loginAndGetToken();

        String requestBody = """
            {
                "userId": "test-user-001",
                "items": [{"productId": "PROD-001", "quantity": 2}]
            }
            """;

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderFlow_whenOrderExists_returnsOrder() throws Exception {
        String token = loginAndGetToken();

        // 先创建订单
        String requestBody = """
            {
                "userId": "test-user-001",
                "items": [{"productId": "PROD-001", "quantity": 1}]
            }
            """;

        var createResult = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // 从响应中提取订单号
        String response = createResult.getResponse().getContentAsString();
        String orderNo = extractOrderNo(response);

        // 查询订单
        authenticatedGet("/api/orders/no/" + orderNo)
                .withToken(token)
                .execute(status().isOk());
    }

    @Test
    void listOrdersFlow_returnsOrderList() throws Exception {
        String token = loginAndGetToken();

        authenticatedGet("/api/orders/user/test-user-001")
                .withToken(token)
                .execute(status().isOk());
    }

    private String extractOrderNo(String jsonResponse) {
        int noStart = jsonResponse.indexOf("\"orderNo\":\"") + 11;
        int noEnd = jsonResponse.indexOf("\"", noStart);
        return jsonResponse.substring(noStart, noEnd);
    }
}
