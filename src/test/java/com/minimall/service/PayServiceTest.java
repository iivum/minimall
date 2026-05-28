package com.minimall.service;

import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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
    void createUnifiedOrder_returnsPrepayId() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100));

        String result = payService.createUnifiedOrder(order, "openid-123");

        assertNotNull(result);
        assertTrue(result.startsWith("prepay_"));
    }

    @Test
    void getJsApiSign_throwsWhenPrivateKeyNotConfigured() {
        when(weChatPayConfig.getMchid()).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
            payService.getJsApiSign("prepay_id", System.currentTimeMillis() / 1000, "nonce"));
    }

    @Test
    void verifyCallback_returnsFalseWhenSerialNoMismatch() {
        when(weChatPayConfig.getSerialNo()).thenReturn("expected-serial");

        boolean result = payService.verifyCallback("{}", "signature", "wrong-serial");

        assertFalse(result);
    }

    @Test
    void verifyCallback_returnsFalseWhenSerialNoIsNull() {
        when(weChatPayConfig.getSerialNo()).thenReturn("expected-serial");

        boolean result = payService.verifyCallback("{}", "signature", null);

        assertFalse(result);
    }
}