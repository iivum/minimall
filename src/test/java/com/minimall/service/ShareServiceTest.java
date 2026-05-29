package com.minimall.service;

import com.minimall.dto.ShareRequest;
import com.minimall.dto.ShareResponse;
import com.minimall.model.Product;
import com.minimall.model.ShareReward;
import com.minimall.model.User;
import com.minimall.repository.ProductRepository;
import com.minimall.repository.ShareRewardRepository;
import com.minimall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private ShareRewardRepository shareRewardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    private ShareService shareService;

    @BeforeEach
    void setUp() {
        shareService = new ShareService(shareRewardRepository, userRepository, productRepository);
    }

    @Test
    @DisplayName("createShareLink creates share and returns response")
    void createShareLink_success() {
        User user = new User();
        user.setId("user-1");
        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");

        ShareRequest request = new ShareRequest("prod-1", "WECHAT");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        ShareResponse response = shareService.createShareLink("user-1", request);

        assertNotNull(response);
        assertNotNull(response.shareId());
        assertNotNull(response.shareUrl());
        assertNotNull(response.posterUrl());
        assertNotNull(response.expiresAt());
        assertTrue(response.shareUrl().contains("minimall.com/share/"));
        verify(shareRewardRepository).save(any(ShareReward.class));
    }

    @Test
    @DisplayName("createShareLink throws when user not found")
    void createShareLink_userNotFound() {
        ShareRequest request = new ShareRequest("prod-1", "WECHAT");
        when(userRepository.findById("invalid-user")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> shareService.createShareLink("invalid-user", request));
    }

    @Test
    @DisplayName("createShareLink throws when product not found")
    void createShareLink_productNotFound() {
        User user = new User();
        user.setId("user-1");
        ShareRequest request = new ShareRequest("invalid-prod", "WECHAT");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(productRepository.findById("invalid-prod")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> shareService.createShareLink("user-1", request));
    }

    @Test
    @DisplayName("getUserRewards returns paginated rewards")
    void getUserRewards_paginated() {
        User user = new User();
        user.setId("user-1");
        ShareReward reward = new ShareReward();
        reward.setId("reward-1");
        reward.setSharer(user);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ShareReward> page = new PageImpl<>(List.of(reward), pageable, 1);

        when(shareRewardRepository.findBySharerId("user-1", pageable)).thenReturn(page);

        Page<ShareReward> result = shareService.getUserRewards("user-1", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("reward-1", result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("getUserRewards returns list of rewards")
    void getUserRewards_list() {
        User user = new User();
        user.setId("user-1");
        ShareReward reward = new ShareReward();
        reward.setId("reward-1");
        reward.setSharer(user);

        when(shareRewardRepository.findBySharerId("user-1")).thenReturn(List.of(reward));

        List<ShareReward> result = shareService.getUserRewards("user-1");

        assertEquals(1, result.size());
        assertEquals("reward-1", result.get(0).getId());
    }

    @Test
    @DisplayName("recordShareEffect creates reward with calculated amount")
    void recordShareEffect_createsReward() {
        User sharer = new User();
        sharer.setId("sharer-1");

        when(userRepository.findById("sharer-1")).thenReturn(Optional.of(sharer));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        shareService.recordShareEffect("sharer-1", "order-123", new BigDecimal("100.00"));

        ArgumentCaptor<ShareReward> captor = ArgumentCaptor.forClass(ShareReward.class);
        verify(shareRewardRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("5.00").compareTo(captor.getValue().getRewardAmount()));
        assertEquals("order-123", captor.getValue().getOrderId());
        assertEquals(ShareReward.RewardType.CASH, captor.getValue().getRewardType());
    }

    @Test
    @DisplayName("recordShareEffect throws when sharer not found")
    void recordShareEffect_sharerNotFound() {
        when(userRepository.findById("invalid-sharer")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> shareService.recordShareEffect("invalid-sharer", "order-123", new BigDecimal("100.00")));
    }
}