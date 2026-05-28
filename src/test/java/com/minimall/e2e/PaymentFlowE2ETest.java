package com.minimall.e2e;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 支付流程 E2E 测试
 *
 * <p>测试支付初始化、状态查询、回调等支付相关功能。
 * 使用 E2ETestBase 提供的标准化认证流程。
 */
class PaymentFlowE2ETest extends E2ETestBase {

    @Test
    void initiatePaymentFlow_withValidOrder_returnsPaymentInfo() throws Exception {
        String token = loginAndGetToken();

        authenticatedPost("/api/payments/initiate")
                .withToken(token)
                .withBody(new PaymentRequest("ORD-001", "99.99"))
                .execute(status().isOk());
    }

    @Test
    void getPaymentStatusFlow_whenPaymentExists_returnsStatus() throws Exception {
        String token = loginAndGetToken();

        authenticatedGet("/api/payments/status/PAY-001")
                .withToken(token)
                .execute(status().isOk());
    }

    @Test
    void callbackPaymentFlow_withValidSignature_returnsSuccess() throws Exception {
        String token = loginAndGetToken();

        authenticatedPost("/api/payments/callback")
                .withToken(token)
                .withBody(new CallbackRequest("TXN-12345", "SUCCESS"))
                .execute(status().isOk());
    }

    public static class PaymentRequest {
        public String orderId;
        public String amount;

        public PaymentRequest(String orderId, String amount) {
            this.orderId = orderId;
            this.amount = amount;
        }
    }

    public static class CallbackRequest {
        public String transactionId;
        public String status;

        public CallbackRequest(String transactionId, String status) {
            this.transactionId = transactionId;
            this.status = status;
        }
    }
}
