package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import com.minimall.service.PayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayControllerTest {

    @Mock
    private PayService payService;

    @Mock
    private OrderService orderService;

    private PayController controller;

    @BeforeEach
    void setUp() {
        controller = new PayController(payService, orderService);
    }

    @Test
    void createPayRequest_returnsPayParameters() {
        Order order = new Order();
        order.setId("order-1");
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100));

        when(orderService.findById("order-1")).thenReturn(order);
        when(payService.createUnifiedOrder(order, "openid-123")).thenReturn("prepay_id_test");
        when(payService.getJsApiSign(anyString(), anyLong(), anyString())).thenReturn("test-signature");

        ResponseEntity<Map<String, Object>> response = controller.createPayRequest("order-1", "openid-123");

        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("timeStamp"));
        assertTrue(response.getBody().containsKey("nonceStr"));
        assertTrue(response.getBody().containsKey("package"));
        assertTrue(response.getBody().containsKey("signType"));
        assertTrue(response.getBody().containsKey("paySign"));
        assertEquals("prepay_id=prepay_id_test", response.getBody().get("package"));
        assertEquals("RSA", response.getBody().get("signType"));
    }

    @Test
    void createPayRequest_callsOrderService() {
        Order order = new Order();
        order.setId("order-1");
        when(orderService.findById("order-1")).thenReturn(order);
        when(payService.createUnifiedOrder(order, "openid")).thenReturn("prepay_id");
        when(payService.getJsApiSign(anyString(), anyLong(), anyString())).thenReturn("sign");

        controller.createPayRequest("order-1", "openid");

        verify(orderService).findById("order-1");
        verify(payService).createUnifiedOrder(order, "openid");
    }

    @Test
    void handleCallback_returnsBadRequest_whenVerificationFails() {
        when(payService.verifyCallback("{}", "bad-signature", "serial")).thenReturn(false);

        ResponseEntity<String> response = controller.handleCallback("{}", "bad-signature", "serial");

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VERIFY_FAILED", response.getBody());
    }

    @Test
    void handleCallback_returnsSuccess_whenProcessingSucceeds() {
        when(payService.verifyCallback("{}", "signature", "serial")).thenReturn(true);
        doNothing().when(payService).processCallback("{}");

        ResponseEntity<String> response = controller.handleCallback("{}", "signature", "serial");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("SUCCESS", response.getBody());
    }

    @Test
    void handleCallback_returnsFail_whenProcessingThrowsException() {
        when(payService.verifyCallback("{}", "signature", "serial")).thenReturn(true);
        doThrow(new RuntimeException("Processing failed")).when(payService).processCallback("{}");

        ResponseEntity<String> response = controller.handleCallback("{}", "signature", "serial");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("FAIL", response.getBody());
    }
}