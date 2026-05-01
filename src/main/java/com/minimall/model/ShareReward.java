package com.minimall.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "share_rewards")
public class ShareReward {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sharer_id", nullable = false)
    private User sharer;

    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "reward_amount", precision = 10, scale = 2)
    private BigDecimal rewardAmount;

    @Column(name = "coupon_id")
    private String couponId;

    @Column(name = "order_id")
    private String orderId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    public enum RewardType {
        CASH, COUPON
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getSharer() { return sharer; }
    public void setSharer(User sharer) { this.sharer = sharer; }
    public RewardType getRewardType() { return rewardType; }
    public void setRewardType(RewardType rewardType) { this.rewardType = rewardType; }
    public BigDecimal getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(BigDecimal rewardAmount) { this.rewardAmount = rewardAmount; }
    public String getCouponId() { return couponId; }
    public void setCouponId(String couponId) { this.couponId = couponId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getClaimedAt() { return claimedAt; }
    public void setClaimedAt(Instant claimedAt) { this.claimedAt = claimedAt; }
}
