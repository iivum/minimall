package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeChatSubscribeServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private WeChatSubscribeConfig config;

    private WeChatSubscribeService service;

    @BeforeEach
    void setUp() {
        lenient().when(config.getAppId()).thenReturn("test-app-id");
        lenient().when(config.getAppSecret()).thenReturn("test-app-secret");
        lenient().when(config.getOrderCreatedTemplateId()).thenReturn("template-1");
        lenient().when(config.getOrderPaidTemplateId()).thenReturn("template-2");
        lenient().when(config.getOrderShippedTemplateId()).thenReturn("template-3");
        lenient().when(config.getOrderCompletedTemplateId()).thenReturn("template-4");

        service = new WeChatSubscribeService(subscriptionRepository, config);
    }

    @Test
    void sendOrderCreatedMessage_doesNothing_whenNoSubscription() {
        when(subscriptionRepository.findByOpenid("openid")).thenReturn(Optional.empty());

        User user = new User();
        user.setOpenid("openid");
        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setTotalAmount(BigDecimal.valueOf(100));

        service.sendOrderCreatedMessage(order, user);

        verify(subscriptionRepository).findByOpenid("openid");
    }

    @Test
    void sendOrderCreatedMessage_doesNothing_whenSubscriptionDisabled() {
        UserSubscription sub = new UserSubscription();
        sub.setOrderCreatedEnabled(false);
        when(subscriptionRepository.findByOpenid("openid")).thenReturn(Optional.of(sub));

        User user = new User();
        user.setOpenid("openid");
        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setTotalAmount(BigDecimal.valueOf(100));

        service.sendOrderCreatedMessage(order, user);

        verify(subscriptionRepository).findByOpenid("openid");
    }

    @Test
    void sendOrderPaidMessage_doesNothing_whenNoSubscription() {
        when(subscriptionRepository.findByOpenid("openid")).thenReturn(Optional.empty());

        User user = new User();
        user.setOpenid("openid");
        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setTotalAmount(BigDecimal.valueOf(100));

        service.sendOrderPaidMessage(order, user);

        verify(subscriptionRepository).findByOpenid("openid");
    }

    @Test
    void sendOrderShippedMessage_doesNothing_whenNoSubscription() {
        when(subscriptionRepository.findByOpenid("openid")).thenReturn(Optional.empty());

        User user = new User();
        user.setOpenid("openid");
        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setTotalAmount(BigDecimal.valueOf(100));

        service.sendOrderShippedMessage(order, user, "EXPRESS123");

        verify(subscriptionRepository).findByOpenid("openid");
    }

    @Test
    void sendOrderCompletedMessage_doesNothing_whenNoSubscription() {
        when(subscriptionRepository.findByOpenid("openid")).thenReturn(Optional.empty());

        User user = new User();
        user.setOpenid("openid");
        Order order = new Order();
        order.setOrderNo("ORDER123");
        order.setTotalAmount(BigDecimal.valueOf(100));

        service.sendOrderCompletedMessage(order, user);

        verify(subscriptionRepository).findByOpenid("openid");
    }

    @Test
    void TemplateData_record_works() {
        var data = new WeChatSubscribeService.TemplateData("test-value");
        assertEquals("test-value", data.value());
    }
}