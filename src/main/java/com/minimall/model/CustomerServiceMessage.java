package com.minimall.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customer_service_messages")
public class CustomerServiceMessage {

    public enum MessageType {
        TEXT, IMAGE, VOICE, VIDEO, LINK
    }

    public enum Status {
        PENDING, PROCESSING, COMPLETED, TRANSFERRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String openid;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "from_customer", nullable = false)
    private boolean fromCustomer = false;

    @Column(name = "handler_id")
    private String handlerId;

    @Column(length = 4000)
    private String reply;

    @Column(name = "auto_reply_rule")
    private String autoReplyRule;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "transferred_at")
    private Instant transferredAt;

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

    public void markAsRead() {
        this.status = Status.PROCESSING;
        this.readAt = Instant.now();
    }

    public void complete() {
        this.status = Status.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void transferToHuman(String handlerId) {
        this.status = Status.TRANSFERRED;
        this.handlerId = handlerId;
        this.transferredAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public boolean isFromCustomer() { return fromCustomer; }
    public void setFromCustomer(boolean fromCustomer) { this.fromCustomer = fromCustomer; }
    public String getHandlerId() { return handlerId; }
    public void setHandlerId(String handlerId) { this.handlerId = handlerId; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getAutoReplyRule() { return autoReplyRule; }
    public void setAutoReplyRule(String autoReplyRule) { this.autoReplyRule = autoReplyRule; }
    public Instant getReadAt() { return readAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getTransferredAt() { return transferredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
