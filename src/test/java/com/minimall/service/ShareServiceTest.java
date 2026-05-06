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

import java.math.BigDecimal;
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
    @DisplayName("createShareLink creates share link and reward")
    void createShareLink_createsShareLinkAndReward() {
        User user = new User();
        user.setId("user-1");

        Product product = new Product();
        product.setId("prod-1");

        ShareRequest request = new ShareRequest("prod-1", "WECHAT");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        ShareResponse response = shareService.createShareLink("user-1", request);

        assertNotNull(response);
        assertNotNull(response.shareId());
        assertNotNull(response.shareUrl());
        assertTrue(response.shareUrl().startsWith("https://minimall.com/share/"));
        verify(shareRewardRepository).save(any(ShareReward.class));
    }

    @Test
    @DisplayName("createShareLink throws when user not found")
    void createShareLink_userNotFound_throws() {
        ShareRequest request = new ShareRequest("prod-1", "WECHAT");
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("invalid", request));
    }

    @Test
    @DisplayName("getUserRewards returns rewards for user")
    void getUserRewards_returnsRewards() {
        ShareReward reward = new ShareReward();
        reward.setId("reward-1");

        when(shareRewardRepository.findBySharerId("user-1")).thenReturn(List.of(reward));

        List<ShareReward> result = shareService.getUserRewards("user-1");

        assertEquals(1, result.size());
        assertEquals("reward-1", result.get(0).getId());
    }

    @Test
    @DisplayName("recordShareEffect records share with order")
    void recordShareEffect_recordsShareWithOrder() {
        User sharer = new User();
        sharer.setId("user-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        shareService.recordShareEffect("user-1", "order-1", new BigDecimal("100"));

        verify(shareRewardRepository).save(any(ShareReward.class));
    }
}