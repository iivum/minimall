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
    void createShareLink_createsShareLinkSuccessfully() {
        User sharer = new User();
        sharer.setId("user-1");
        sharer.setNickname("Test Sharer");

        Product product = new Product();
        product.setId("prod-1");
        product.setName("Test Product");

        ShareRequest request = new ShareRequest("prod-1", "default");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        ShareResponse result = shareService.createShareLink("user-1", request);

        assertNotNull(result);
        assertNotNull(result.shareId());
        assertNotNull(result.shareUrl());
        assertTrue(result.shareUrl().contains("minimall.com/share/"));
        assertNotNull(result.expiresAt());
        verify(shareRewardRepository).save(any(ShareReward.class));
    }

    @Test
    void createShareLink_throwsWhenUserNotFound() {
        ShareRequest request = new ShareRequest("prod-1", "default");
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("unknown", request));
    }

    @Test
    void createShareLink_throwsWhenProductNotFound() {
        User sharer = new User();
        sharer.setId("user-1");

        ShareRequest request = new ShareRequest("unknown", "default");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(productRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.createShareLink("user-1", request));
    }

    @Test
    void getUserRewards_returnsRewardsForUser() {
        ShareReward reward = new ShareReward();
        reward.setId("reward-1");
        reward.setRewardAmount(BigDecimal.valueOf(5.00));

        when(shareRewardRepository.findBySharerId("user-1")).thenReturn(List.of(reward));

        List<ShareReward> result = shareService.getUserRewards("user-1");

        assertEquals(1, result.size());
        assertEquals("reward-1", result.get(0).getId());
    }

    @Test
    void recordShareEffect_recordsRewardWithCalculatedAmount() {
        User sharer = new User();
        sharer.setId("user-1");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(sharer));
        when(shareRewardRepository.save(any(ShareReward.class))).thenAnswer(i -> i.getArgument(0));

        shareService.recordShareEffect("user-1", "order-123", BigDecimal.valueOf(100.00));

        verify(shareRewardRepository).save(argThat(reward ->
            reward.getOrderId().equals("order-123") &&
            reward.getRewardAmount().compareTo(BigDecimal.valueOf(5.00)) == 0
        ));
    }

    @Test
    void recordShareEffect_throwsWhenSharerNotFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            shareService.recordShareEffect("unknown", "order-123", BigDecimal.valueOf(100.00)));
    }
}