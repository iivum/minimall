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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock private ShareRewardRepository shareRewardRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    private ShareService shareService;

    @BeforeEach
    void setUp() {
        shareService = new ShareService(shareRewardRepository, userRepository, productRepository);
    }

    @Test
    void createShareLink_success() {
        User sharer = new User();
        sharer.setId("user-1");
        sharer.setNickname("TestUser");

        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");

        ShareRequest request = new ShareRequest("prod-1", "wechat");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(shareRewardRepository.save(any(ShareReward.class))).thenReturn(new ShareReward());

        ShareResponse response = shareService.createShareLink("user-1", request);

        assertNotNull(response);
        assertNotNull(response.shareId());
        assertNotNull(response.shareUrl());
        assertTrue(response.shareUrl().startsWith("https://minimall.com/share/"));
        assertNotNull(response.expiresAt());
        verify(shareRewardRepository).save(any(ShareReward.class));
    }

    @Test
    void createShareLink_throwsWhenUserNotFound() {
        ShareRequest request = new ShareRequest("prod-1", "wechat");
        when(userRepository.findById("invalid-user")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("invalid-user", request)
        );
    }

    @Test
    void createShareLink_throwsWhenProductNotFound() {
        User sharer = new User();
        sharer.setId("user-1");

        ShareRequest request = new ShareRequest("invalid-prod", "wechat");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(productRepository.findById("invalid-prod")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("user-1", request)
        );
    }

    @Test
    void getUserRewards_returnsRewards() {
        User sharer = new User();
        sharer.setId("user-1");

        ShareReward reward = new ShareReward();
        reward.setId("reward-1");
        reward.setSharer(sharer);
        reward.setRewardAmount(BigDecimal.valueOf(5));

        when(shareRewardRepository.findBySharerId("user-1")).thenReturn(List.of(reward));

        List<ShareReward> result = shareService.getUserRewards("user-1");

        assertEquals(1, result.size());
        assertEquals("reward-1", result.get(0).getId());
    }

    @Test
    void getUserRewards_withPagination_returnsPagedRewards() {
        User sharer = new User();
        sharer.setId("user-1");

        ShareReward reward = new ShareReward();
        reward.setId("reward-1");
        reward.setSharer(sharer);

        Page<ShareReward> page = new PageImpl<>(List.of(reward));
        when(shareRewardRepository.findBySharerId(eq("user-1"), any(PageRequest.class))).thenReturn(page);

        Page<ShareReward> result = shareService.getUserRewards("user-1", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void recordShareEffect_success() {
        User sharer = new User();
        sharer.setId("user-1");

        ShareReward savedReward = new ShareReward();
        savedReward.setId("reward-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(shareRewardRepository.save(any(ShareReward.class))).thenReturn(savedReward);

        BigDecimal orderAmount = BigDecimal.valueOf(100);
        shareService.recordShareEffect("user-1", "order-1", orderAmount);

        verify(shareRewardRepository).save(any(ShareReward.class));
    }

    @Test
    void recordShareEffect_throwsWhenSharerNotFound() {
        when(userRepository.findById("invalid-user")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.recordShareEffect("invalid-user", "order-1", BigDecimal.valueOf(100))
        );
    }
}