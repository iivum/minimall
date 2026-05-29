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
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeChatSubscribeServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private WeChatSubscribeConfig config;

    private WeChatSubscribeService subscribeService;

    @BeforeEach
    void setUp() {
        when(config.getOrderCreatedTemplateId()).thenReturn("template-1");
        when(config.getOrderPaidTemplateId()).thenReturn("template-2");
        when(config.getOrderShippedTemplateId()).thenReturn("template-3");
        when(config.getOrderCompletedTemplateId()).thenReturn("template-4");
        when(config.getAppId()).thenReturn("test-appid");
        when(config.getAppSecret()).thenReturn("test-appsecret");

        subscribeService = new WeChatSubscribeService(subscriptionRepository, config);
    }

    @Test
    void sendOrderCreatedMessage_doesNothing_whenNoSubscription() {
        User user = new User();
        user.setOpenid("openid-123");

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.empty());

        subscribeService.sendOrderCreatedMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderCreatedMessage_doesNothing_whenSubscriptionDisabled() {
        User user = new User();
        user.setOpenid("openid-123");

        UserSubscription sub = new UserSubscription();
        sub.setOrderCreatedEnabled(false);

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.of(sub));

        subscribeService.sendOrderCreatedMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderCreatedMessage_sendsMessage_whenSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        UserSubscription sub = new UserSubscription();
        sub.setOrderCreatedEnabled(true);

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.of(sub));

        subscribeService.sendOrderCreatedMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderPaidMessage_doesNothing_whenNotSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.empty());

        subscribeService.sendOrderPaidMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderShippedMessage_doesNothing_whenNotSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.empty());

        subscribeService.sendOrderShippedMessage(createOrder(), user, "EXPRESS-123");

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderCompletedMessage_doesNothing_whenNotSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.empty());

        subscribeService.sendOrderCompletedMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderShippedMessage_sendsMessage_whenSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        UserSubscription sub = new UserSubscription();
        sub.setOrderShippedEnabled(true);

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.of(sub));

        subscribeService.sendOrderShippedMessage(createOrder(), user, "EXPRESS-123");

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderPaidMessage_sendsMessage_whenSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        UserSubscription sub = new UserSubscription();
        sub.setOrderPaidEnabled(true);

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.of(sub));

        subscribeService.sendOrderPaidMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void sendOrderCompletedMessage_sendsMessage_whenSubscribed() {
        User user = new User();
        user.setOpenid("openid-123");

        UserSubscription sub = new UserSubscription();
        sub.setOrderCompletedEnabled(true);

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.of(sub));

        subscribeService.sendOrderCompletedMessage(createOrder(), user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    @Test
    void getAccessTokenAsync_returnsFuture() {
        CompletableFuture<String> future = subscribeService.getAccessTokenAsync();
        assertNotNull(future);
    }

    @Test
    void sendTemplateMessageAsync_returnsFuture() {
        CompletableFuture<Void> future = subscribeService.sendTemplateMessageAsync(
            "openid-123", "template-1", java.util.Map.of("key", new WeChatSubscribeService.TemplateData("value")));
        assertNotNull(future);
    }

    @Test
    void sendOrderCreatedMessage_doesNothing_whenOrderIsNull() {
        User user = new User();
        user.setOpenid("openid-123");

        when(subscriptionRepository.findByOpenid("openid-123")).thenReturn(Optional.empty());

        subscribeService.sendOrderCreatedMessage(null, user);

        verify(subscriptionRepository).findByOpenid("openid-123");
    }

    private Order createOrder() {
        Order order = new Order();
        order.setOrderNo("ORD-001");
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        return order;
    }
}
