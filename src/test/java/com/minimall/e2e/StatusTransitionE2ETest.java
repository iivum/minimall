package com.minimall.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class StatusTransitionE2ETest {

    private static String baseUrl;
    private String testOpenid;
    private String messageId;

    @BeforeAll
    static void beforeAll() {
        baseUrl = System.getProperty("base.url", "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        testOpenid = "e2e_status_" + UUID.randomUUID().toString().substring(0, 8);
        Response response = RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .param("openid", testOpenid)
            .param("content", "状态测试消息")
            .param("type", "TEXT")
        .when()
            .post(baseUrl + "/api/customer-service/receive");

        messageId = response.jsonPath().getString("id");
    }

    @Test
    @DisplayName("状态流转E2E测试 - 待处理 -> 处理中 -> 已完成")
    void testStatusTransition() {
        Response readResponse = RestAssured.given()
        .when()
            .post(baseUrl + "/api/customer-service/" + messageId + "/read");

        assertEquals(200, readResponse.statusCode());
        assertEquals("PROCESSING", readResponse.jsonPath().getString("status"));

        Response completeResponse = RestAssured.given()
        .when()
            .post(baseUrl + "/api/customer-service/" + messageId + "/complete");

        assertEquals(200, completeResponse.statusCode());
        assertEquals("COMPLETED", completeResponse.jsonPath().getString("status"));
        assertNotNull(completeResponse.jsonPath().getString("completedAt"));
    }
}
