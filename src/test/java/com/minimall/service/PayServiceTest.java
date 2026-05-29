package com.minimall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayServiceTest {

    @Mock
    private WeChatPayConfig weChatPayConfig;

    @Mock
    private OrderService orderService;

    private PayService payService;

    @BeforeEach
    void setUp() {
        payService = new PayService(weChatPayConfig, orderService);
    }

    @Test
    void createUnifiedOrder_generatesPrepayId() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100));

        String prepayId = payService.createUnifiedOrder(order, "openid-123");

        assertNotNull(prepayId);
        assertTrue(prepayId.startsWith("prepay_"));
    }

    @Test
    void verifyCallback_returnsFalse_whenSerialNoMismatch() {
        when(weChatPayConfig.getSerialNo()).thenReturn("expected-serial");

        boolean result = payService.verifyCallback("{}", "signature", "wrong-serial");

        assertFalse(result);
    }

    @Test
    void verifyCallback_returnsFalse_whenSerialNoIsNull() {
        boolean result = payService.verifyCallback("{}", "signature", null);

        assertFalse(result);
    }

    @Test
    void getJsApiSign_throwsException_whenPrivateKeyNotConfigured() {
        when(weChatPayConfig.getMchid()).thenReturn("mchid");
        when(weChatPayConfig.getPrivateKeyContent()).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
            payService.getJsApiSign("prepay_id", System.currentTimeMillis(), "nonce"));
    }

    @Test
    void getJsApiSign_throwsException_whenPrivateKeyEmpty() {
        when(weChatPayConfig.getMchid()).thenReturn("mchid");
        when(weChatPayConfig.getPrivateKeyContent()).thenReturn("");

        assertThrows(RuntimeException.class, () ->
            payService.getJsApiSign("prepay_id", System.currentTimeMillis(), "nonce"));
    }

    @Test
    void processCallback_handlesSuccessfulPayment() {
        Order order = new Order();
        order.setId("order-id-1");
        order.setOrderNo("ORD-001");

        Map<String, Object> resource = new HashMap<>();
        resource.put("out_trade_no", "ORD-001");
        resource.put("transaction_id", "TXN-123");
        Map<String, Object> amount = new HashMap<>();
        amount.put("state", "SUCCESS");
        resource.put("amount", amount);

        Map<String, Object> notification = new HashMap<>();
        notification.put("resource", resource);

        try {
            String body = new ObjectMapper().writeValueAsString(notification);
            when(orderService.findByOrderNo("ORD-001")).thenReturn(order);

            payService.processCallback(body);

            verify(orderService).pay("order-id-1", "TXN-123");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void processCallback_handlesFailedPayment() {
        Order order = new Order();
        order.setId("order-id-1");
        order.setOrderNo("ORD-001");

        Map<String, Object> resource = new HashMap<>();
        resource.put("out_trade_no", "ORD-001");
        resource.put("transaction_id", "TXN-123");
        Map<String, Object> amount = new HashMap<>();
        amount.put("state", "FAIL");
        resource.put("amount", amount);

        Map<String, Object> notification = new HashMap<>();
        notification.put("resource", resource);

        try {
            String body = new ObjectMapper().writeValueAsString(notification);
            when(orderService.findByOrderNo("ORD-001")).thenReturn(order);

            payService.processCallback(body);

            verify(orderService, never()).pay(anyString(), anyString());
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void processCallback_throwsException_onInvalidJson() {
        String invalidBody = "not valid json";

        assertThrows(RuntimeException.class, () -> payService.processCallback(invalidBody));
    }

    @Test
    void processCallback_throwsException_whenResourceIsNull() {
        String body = "{}";

        assertThrows(RuntimeException.class, () -> payService.processCallback(body));
    }
}
