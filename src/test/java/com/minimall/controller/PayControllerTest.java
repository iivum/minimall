package com.minimall.controller;

import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.service.OrderService;
import com.minimall.service.PayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayController.class)
class PayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayService payService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private WeChatPayConfig weChatPayConfig;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setNickname("TestUser");

        testOrder = new Order();
        testOrder.setId("order-456");
        testOrder.setOrderNo("ORDER-001");
        testOrder.setUser(testUser);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.00));
        testOrder.setStatus(Order.Status.PENDING);
        testOrder.setPayStatus(Order.PayStatus.UNPAID);
    }

    @Test
    @WithMockUser
    void createPayRequest_withValidOrderAndOpenid_returnsPrepayInfo() throws Exception {
        when(orderService.findById("order-456")).thenReturn(testOrder);
        when(payService.createUnifiedOrder(testOrder, "openid-abc")).thenReturn("prepay_id_123");
        when(payService.getJsApiSign(eq("prepay_id_123"), any(Long.class), any(String.class)))
            .thenReturn("mock_signature");

        mockMvc.perform(post("/api/pay/create/order-456")
                .with(csrf())
                .param("openid", "openid-abc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timeStamp").exists())
            .andExpect(jsonPath("$.nonceStr").exists())
            .andExpect(jsonPath("$.package").value("prepay_id=prepay_id_123"))
            .andExpect(jsonPath("$.signType").value("RSA"))
            .andExpect(jsonPath("$.paySign").value("mock_signature"));
    }

    @Test
    @WithMockUser
    void createPayRequest_withDifferentOrderId_returnsPrepayInfo() throws Exception {
        Order anotherOrder = new Order();
        anotherOrder.setId("order-789");
        anotherOrder.setOrderNo("ORDER-002");
        anotherOrder.setUser(testUser);
        anotherOrder.setTotalAmount(BigDecimal.valueOf(200.00));

        when(orderService.findById("order-789")).thenReturn(anotherOrder);
        when(payService.createUnifiedOrder(anotherOrder, "openid-xyz")).thenReturn("prepay_id_456");
        when(payService.getJsApiSign(eq("prepay_id_456"), any(Long.class), any(String.class)))
            .thenReturn("signature_456");

        mockMvc.perform(post("/api/pay/create/order-789")
                .with(csrf())
                .param("openid", "openid-xyz"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.package").value("prepay_id=prepay_id_456"));
    }

    @Test
    void createPayRequest_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/pay/create/order-456")
                .with(csrf())
                .param("openid", "openid-abc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void handleCallback_withValidSignature_returnsSuccess() throws Exception {
        String validBody = "{\"resource\":{\"out_trade_no\":\"ORDER-001\",\"transaction_id\":\"TX123\",\"amount\":{\"state\":\"SUCCESS\"}}}";
        when(payService.verifyCallback(validBody, "valid_sig", "serial_123")).thenReturn(true);

        mockMvc.perform(post("/api/pay/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody)
                .header("Wechatpay-Signature", "valid_sig")
                .header("Wechatpay-Serial", "serial_123"))
            .andExpect(status().isOk())
            .andExpect(content().string("SUCCESS"));
    }

    @Test
    @WithMockUser
    void handleCallback_withInvalidSignature_returnsBadRequest() throws Exception {
        String invalidBody = "{\"resource\":{\"out_trade_no\":\"ORDER-001\"}}";
        when(payService.verifyCallback(invalidBody, "invalid_sig", "serial_123")).thenReturn(false);

        mockMvc.perform(post("/api/pay/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody)
                .header("Wechatpay-Signature", "invalid_sig")
                .header("Wechatpay-Serial", "serial_123"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("VERIFY_FAILED"));
    }

    @Test
    @WithMockUser
    void handleCallback_withValidSignature_butProcessException_returnsFail() throws Exception {
        String validBody = "{\"resource\":{\"out_trade_no\":\"ORDER-001\",\"transaction_id\":\"TX123\",\"amount\":{\"state\":\"SUCCESS\"}}}";
        when(payService.verifyCallback(validBody, "valid_sig", "serial_123")).thenReturn(true);
        doThrow(new RuntimeException("Processing failed")).when(payService).processCallback(validBody);

        mockMvc.perform(post("/api/pay/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody)
                .header("Wechatpay-Signature", "valid_sig")
                .header("Wechatpay-Serial", "serial_123"))
            .andExpect(status().isOk())
            .andExpect(content().string("FAIL"));
    }

    @Test
    @WithMockUser
    void handleCallback_withMissingSignatureHeader_returnsBadRequest() throws Exception {
        String body = "{\"resource\":{\"out_trade_no\":\"ORDER-001\"}}";

        mockMvc.perform(post("/api/pay/callback")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    }