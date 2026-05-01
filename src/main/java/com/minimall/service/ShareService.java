package com.minimall.service;

import com.minimall.dto.ShareRequest;
import com.minimall.dto.ShareResponse;
import com.minimall.model.Product;
import com.minimall.model.ShareReward;
import com.minimall.model.User;
import com.minimall.repository.ProductRepository;
import com.minimall.repository.ShareRewardRepository;
import com.minimall.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ShareService {
    private final ShareRewardRepository shareRewardRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private static final BigDecimal DEFAULT_REWARD_AMOUNT = new BigDecimal("5.00");
    private static final String SHARE_BASE_URL = "https://minimall.com/share/";

    public ShareService(ShareRewardRepository shareRewardRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.shareRewardRepository = shareRewardRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ShareResponse createShareLink(String userId, ShareRequest request) {
        User sharer = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String shareId = UUID.randomUUID().toString();
        String shareUrl = SHARE_BASE_URL + shareId;
        String posterUrl = generatePosterUrl(product, sharer);
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        ShareReward reward = new ShareReward();
        reward.setSharer(sharer);
        reward.setRewardType(ShareReward.RewardType.CASH);
        reward.setRewardAmount(DEFAULT_REWARD_AMOUNT);
        shareRewardRepository.save(reward);

        return new ShareResponse(shareId, shareUrl, posterUrl, expiresAt);
    }

    public List<ShareReward> getUserRewards(String userId) {
        return shareRewardRepository.findBySharerId(userId);
    }

    @Transactional
    public void recordShareEffect(String sharerId, String orderId, BigDecimal orderAmount) {
        User sharer = userRepository.findById(sharerId)
            .orElseThrow(() -> new IllegalArgumentException("Sharer not found"));

        ShareReward reward = new ShareReward();
        reward.setSharer(sharer);
        reward.setRewardType(ShareReward.RewardType.CASH);
        reward.setRewardAmount(orderAmount.multiply(new BigDecimal("0.05")));
        reward.setOrderId(orderId);
        shareRewardRepository.save(reward);
    }

    private String generatePosterUrl(Product product, User sharer) {
        return "https://minimall.com/posters/" + product.getId() + "?from=" + sharer.getId();
    }
}
