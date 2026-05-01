package com.minimall.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_coupons")
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "claimed_at", nullable = false)
    private Instant claimedAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "used_order_id")
    private String usedOrderId;

    @Column(nullable = false)
    private boolean isUsed = false;

    @PrePersist
    protected void onCreate() {
        claimedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public Instant getClaimedAt() { return claimedAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public String getUsedOrderId() { return usedOrderId; }
    public void setUsedOrderId(String usedOrderId) { this.usedOrderId = usedOrderId; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
}
