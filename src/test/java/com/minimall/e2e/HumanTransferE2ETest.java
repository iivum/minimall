package com.minimall.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class HumanTransferE2ETest {

    private static String baseUrl;
    private String testOpenid;
    private String messageId;
    private static final String HANDLER_ID = "handler_001";

    @BeforeAll
    static void beforeAll() {
        baseUrl = System.getProperty("base.url", "http://localhost:8080");
    }

    @BeforeEach
    void setUp() {
        testOpenid = "e2e_transfer_" + UUID.randomUUID().toString().substring(0, 8);
        Response response = RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .param("openid", testOpenid)
            .param("content", "转人工")
            .param("type", "TEXT")
        .when()
            .post(baseUrl + "/api/customer-service/receive");

        messageId = response.jsonPath().getString("id");
    }

    @Test
    @DisplayName("人工转接E2E测试 - 用户请求转人工后消息被标记为已转接")
    void testHumanTransfer() {
        Response response = RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .param("handlerId", HANDLER_ID)
        .when()
            .post(baseUrl + "/api/customer-service/" + messageId + "/transfer");

        assertEquals(200, response.statusCode());
        assertEquals("TRANSFERRED", response.jsonPath().getString("status"));
        assertEquals(HANDLER_ID, response.jsonPath().getString("handlerId"));
        assertNotNull(response.jsonPath().getString("transferredAt"));
    }
}
