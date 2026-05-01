package com.minimall.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "repurchase_reminders")
public class RepurchaseReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private ReminderType reminderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status = ReminderStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "next_send_time")
    private Instant nextSendTime;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "send_count")
    private int sendCount = 0;

    @Column(name = "max_send_count")
    private int maxSendCount = 3;

    @Column(name = "last_sent_template_id")
    private String lastSentTemplateId;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    public enum ReminderType {
        PURCHASE_COMPLETE,
        REPURCHASE_WINDOW,
        INACTIVITY
    }

    public enum ReminderStatus {
        PENDING,
        SENT,
        CANCELLED,
        EXPIRED
    }

    public RepurchaseReminder() {}

    public RepurchaseReminder(ReminderType reminderType, ReminderStatus status) {
        this.reminderType = reminderType;
        this.status = status;
    }

    public boolean canResend() {
        if (isSent() || status == ReminderStatus.CANCELLED || status == ReminderStatus.EXPIRED) {
            return false;
        }
        if (sendCount >= maxSendCount) {
            return false;
        }
        if (nextSendTime != null && Instant.now().isBefore(nextSendTime)) {
            return false;
        }
        return true;
    }

    public void markAsSent() {
        this.sent = true;
        this.sentAt = Instant.now();
        this.status = ReminderStatus.SENT;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }
    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Instant getNextSendTime() { return nextSendTime; }
    public void setNextSendTime(Instant nextSendTime) { this.nextSendTime = nextSendTime; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public int getSendCount() { return sendCount; }
    public void setSendCount(int sendCount) { this.sendCount = sendCount; }
    public int getMaxSendCount() { return maxSendCount; }
    public void setMaxSendCount(int maxSendCount) { this.maxSendCount = maxSendCount; }
    public String getLastSentTemplateId() { return lastSentTemplateId; }
    public void setLastSentTemplateId(String lastSentTemplateId) { this.lastSentTemplateId = lastSentTemplateId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }

    private boolean sent;
}