package com.minimall.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class CustomerServiceMessageE2ETest {

    private static String baseUrl;
    private String testOpenid;

    @BeforeAll
    static void beforeAll() {
        baseUrl = System.getProperty("base.url", "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        testOpenid = "e2e_test_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("客服消息接收E2E测试 - 用户发送消息后系统正确接收并存储")
    void testCustomerMessageReception() {
        Response receiveResponse = RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .param("openid", testOpenid)
            .param("content", "您好，我想咨询订单问题")
            .param("type", "TEXT")
        .when()
            .post(baseUrl + "/api/customer-service/receive");

        assertEquals(200, receiveResponse.statusCode());

        String messageId = receiveResponse.jsonPath().getString("id");
        assertNotNull(messageId);
        assertEquals(testOpenid, receiveResponse.jsonPath().getString("openid"));
        assertEquals("PENDING", receiveResponse.jsonPath().getString("status"));
    }
}
