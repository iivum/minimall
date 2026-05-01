package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for WeChatSubscribeService.
 * Tests subscription checking logic without requiring RestTemplate mocking.
 * Integration tests with actual WeChat API should be done separately.
 */
@ExtendWith(MockitoExtension.class)
class WeChatSubscribeServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private WeChatSubscribeConfig config;

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    private WeChatSubscribeService service;

    private User testUser;
    private Order testOrder;
    private UserSubscription testSubscription;

    @BeforeEach
    void setUp() {
        service = new WeChatSubscribeService(subscriptionRepository, config, restTemplate);

        testUser = new User();
        testUser.setId("user-123");
        testUser.setOpenid("oABC123xyz");

        testOrder = new Order();
        testOrder.setId("order-456");
        testOrder.setOrderNo("ORDER_NO_001");
        testOrder.setTotalAmount(BigDecimal.valueOf(99.99));
        testOrder.setUser(testUser);

        testSubscription = new UserSubscription();
        testSubscription.setOpenid("oABC123xyz");
        testSubscription.setOrderCreatedEnabled(true);
        testSubscription.setOrderPaidEnabled(true);
        testSubscription.setOrderShippedEnabled(true);
        testSubscription.setOrderCompletedEnabled(true);
    }

    @Test
    @DisplayName("sendOrderCreatedMessage does not send when user not subscribed")
    void sendOrderCreatedMessage_notSubscribed() {
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.empty());

        service.sendOrderCreatedMessage(testOrder, testUser);

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderCreatedMessage does not send when subscription disabled")
    void sendOrderCreatedMessage_disabled() {
        testSubscription.setOrderCreatedEnabled(false);
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.of(testSubscription));

        service.sendOrderCreatedMessage(testOrder, testUser);

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderPaidMessage does not send when user not subscribed")
    void sendOrderPaidMessage_notSubscribed() {
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.empty());

        service.sendOrderPaidMessage(testOrder, testUser);

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderPaidMessage does not send when subscription disabled")
    void sendOrderPaidMessage_disabled() {
        testSubscription.setOrderPaidEnabled(false);
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.of(testSubscription));

        service.sendOrderPaidMessage(testOrder, testUser);

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderShippedMessage does not send when user not subscribed")
    void sendOrderShippedMessage_notSubscribed() {
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.empty());

        service.sendOrderShippedMessage(testOrder, testUser, "SF123456789");

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderShippedMessage does not send when subscription disabled")
    void sendOrderShippedMessage_disabled() {
        testSubscription.setOrderShippedEnabled(false);
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.of(testSubscription));

        service.sendOrderShippedMessage(testOrder, testUser, "SF123456789");

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderCompletedMessage does not send when user not subscribed")
    void sendOrderCompletedMessage_notSubscribed() {
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.empty());

        service.sendOrderCompletedMessage(testOrder, testUser);

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("sendOrderCompletedMessage does not send when subscription disabled")
    void sendOrderCompletedMessage_disabled() {
        testSubscription.setOrderCompletedEnabled(false);
        when(subscriptionRepository.findByOpenid("oABC123xyz")).thenReturn(Optional.of(testSubscription));

        service.sendOrderCompletedMessage(testOrder, testUser);

        verify(subscriptionRepository).findByOpenid("oABC123xyz");
        verifyNoInteractions(restTemplate);
    }
}