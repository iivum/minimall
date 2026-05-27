package com.minimall.service;

import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private WeChatPayConfig weChatPayConfig;
    @Mock
    private OrderService orderService;
    @Mock
    private RSAAutoCertificateConfig rsaConfig;

    private PayService payService;

    @BeforeEach
    void setUp() {
        payService = new PayService(weChatPayConfig, orderService, rsaConfig);
    }

    @Test
    void createUnifiedOrder_returnsPrepayId() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        String prepayId = payService.createUnifiedOrder(order, "openid-123");

        assertNotNull(prepayId);
        assertTrue(prepayId.startsWith("prepay_"));
    }

    @Test
    void verifyCallback_returnsFalseWhenSerialNoMismatch() {
        when(weChatPayConfig.getSerialNo()).thenReturn("expected-serial");

        boolean result = payService.verifyCallback("{}", "sig", "wrong-serial");

        assertFalse(result);
    }

    @Test
    void processCallback_doesNotPayWhenStatusNotSuccess() {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");

        when(orderService.findByOrderNo("ORD-001")).thenReturn(order);

        String callbackBody = "{\"resource\":{\"out_trade_no\":\"ORD-001\",\"transaction_id\":\"WX123456\",\"amount\":{\"state\":\"FAILED\"}}}";
        payService.processCallback(callbackBody);

        verify(orderService, never()).pay(any(), any());
    }
}