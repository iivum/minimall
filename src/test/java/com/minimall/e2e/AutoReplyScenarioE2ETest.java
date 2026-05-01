package com.minimall.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class AutoReplyScenarioE2ETest {

    private static String baseUrl;
    private String testOpenid;

    @BeforeAll
    static void beforeAll() {
        baseUrl = System.getProperty("base.url", "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        testOpenid = "e2e_auto_reply_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("自动回复场景测试 - 首次访问触发欢迎语")
    void testFirstVisitWelcomeReply() {
        Response response = RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .param("openid", testOpenid)
            .param("content", "你好")
            .param("type", "TEXT")
        .when()
            .post(baseUrl + "/api/customer-service/receive");

        assertEquals(200, response.statusCode());

        String reply = response.jsonPath().getString("reply");
        assertNotNull(reply);
        assertTrue(reply.contains("欢迎") || reply.contains("帮助"));
    }
}
