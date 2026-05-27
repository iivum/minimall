package com.minimall.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String openid;

    @Column(name = "order_created_enabled", nullable = false)
    private boolean orderCreatedEnabled = false;

    @Column(name = "order_paid_enabled", nullable = false)
    private boolean orderPaidEnabled = false;

    @Column(name = "order_shipped_enabled", nullable = false)
    private boolean orderShippedEnabled = false;

    @Column(name = "order_completed_enabled", nullable = false)
    private boolean orderCompletedEnabled = false;

    @Column(name = "order_refunded_enabled", nullable = false)
    private boolean orderRefundedEnabled = false;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public boolean isOrderCreatedEnabled() { return orderCreatedEnabled; }
    public void setOrderCreatedEnabled(boolean orderCreatedEnabled) { this.orderCreatedEnabled = orderCreatedEnabled; }
    public boolean isOrderPaidEnabled() { return orderPaidEnabled; }
    public void setOrderPaidEnabled(boolean orderPaidEnabled) { this.orderPaidEnabled = orderPaidEnabled; }
    public boolean isOrderShippedEnabled() { return orderShippedEnabled; }
    public void setOrderShippedEnabled(boolean orderShippedEnabled) { this.orderShippedEnabled = orderShippedEnabled; }
    public boolean isOrderCompletedEnabled() { return orderCompletedEnabled; }
    public void setOrderCompletedEnabled(boolean orderCompletedEnabled) { this.orderCompletedEnabled = orderCompletedEnabled; }
    public boolean isOrderRefundedEnabled() { return orderRefundedEnabled; }
    public void setOrderRefundedEnabled(boolean orderRefundedEnabled) { this.orderRefundedEnabled = orderRefundedEnabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}