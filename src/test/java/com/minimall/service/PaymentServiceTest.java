package com.minimall.service;

import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private WeChatPayConfig weChatPayConfig;
    @Mock
    private OrderService orderService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(orderService);
    }

    @Test
    void initiatePayment_generatesPaymentId() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        String paymentId = paymentService.initiatePayment(order, BigDecimal.valueOf(100.00));

        assertNotNull(paymentId);
        assertTrue(paymentId.startsWith("PAY-"));
    }

    @Test
    void getPaymentStatus_returnsPendingStatus() {
        Map<String, Object> status = paymentService.getPaymentStatus("PAY-123");

        assertNotNull(status);
        assertEquals("PAY-123", status.get("paymentId"));
        assertEquals("PENDING", status.get("status"));
        assertEquals("Payment is processing", status.get("message"));
    }

    @Test
    void processCallback_handlesSuccessStatus() {
        assertDoesNotThrow(() -> paymentService.processCallback("TXN-123", "SUCCESS"));
        verify(orderService, never()).pay(any(), any());
    }

    @Test
    void processCallback_handlesFailedStatus() {
        assertDoesNotThrow(() -> paymentService.processCallback("TXN-123", "FAILED"));
        verify(orderService, never()).pay(any(), any());
    }

    @Test
    void processCallback_handlesUnknownStatus() {
        assertDoesNotThrow(() -> paymentService.processCallback("TXN-123", "UNKNOWN"));
        verify(orderService, never()).pay(any(), any());
    }
}