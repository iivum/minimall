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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    @DisplayName("createShareLink creates share link successfully")
    void createShareLink_success() {
        User sharer = new User();
        sharer.setId("user-1");
        sharer.setNickname("Test User");

        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");

        ShareRequest request = new ShareRequest("prod-1", "wechat");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        ShareResponse response = shareService.createShareLink("user-1", request);

        assertNotNull(response);
        assertNotNull(response.shareId());
        assertTrue(response.shareUrl().contains("https://minimall.com/share/"));
        assertTrue(response.posterUrl().contains("prod-1"));
        assertNotNull(response.expiresAt());
        verify(shareRewardRepository).save(any(ShareReward.class));
    }

    @Test
    @DisplayName("createShareLink throws when user not found")
    void createShareLink_userNotFound_throws() {
        ShareRequest request = new ShareRequest("prod-1", "wechat");
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("user-1", request));
    }

    @Test
    @DisplayName("createShareLink throws when product not found")
    void createShareLink_productNotFound_throws() {
        User sharer = new User();
        sharer.setId("user-1");

        ShareRequest request = new ShareRequest("prod-1", "wechat");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(productRepository.findById("prod-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("user-1", request));
    }

    @Test
    @DisplayName("getUserRewards returns rewards for user")
    void getUserRewards_returnsRewards() {
        ShareReward reward = new ShareReward();
        reward.setId("reward-1");
        reward.setRewardAmount(new BigDecimal("5.00"));

        when(shareRewardRepository.findBySharerId("user-1")).thenReturn(List.of(reward));

        List<ShareReward> result = shareService.getUserRewards("user-1");

        assertEquals(1, result.size());
        assertEquals("reward-1", result.get(0).getId());
    }

    @Test
    @DisplayName("getUserRewards with pageable returns paginated rewards")
    void getUserRewards_paginated_returnsPage() {
        ShareReward reward = new ShareReward();
        reward.setId("reward-1");

        Page<ShareReward> page = new PageImpl<>(List.of(reward));
        when(shareRewardRepository.findBySharerId(eq("user-1"), any(PageRequest.class))).thenReturn(page);

        Page<ShareReward> result = shareService.getUserRewards("user-1", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("recordShareEffect records share effect with 5% commission")
    void recordShareEffect_recordsCommission() {
        User sharer = new User();
        sharer.setId("user-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        shareService.recordShareEffect("user-1", "order-123", new BigDecimal("100"));

        verify(shareRewardRepository).save(argThat(reward ->
            reward.getOrderId().equals("order-123") &&
            reward.getRewardAmount().compareTo(new BigDecimal("5.00")) == 0
        ));
    }

    @Test
    @DisplayName("recordShareEffect throws when sharer not found")
    void recordShareEffect_sharerNotFound_throws() {
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.recordShareEffect("user-1", "order-123", new BigDecimal("100")));
    }
}
