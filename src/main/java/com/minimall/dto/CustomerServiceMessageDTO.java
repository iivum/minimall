package com.minimall.dto;

import com.minimall.model.CustomerServiceMessage;

import java.time.Instant;

public class CustomerServiceMessageDTO {
    private String id;
    private String openid;
    private String content;
    private String messageType;
    private String status;
    private boolean fromCustomer;
    private String handlerId;
    private String reply;
    private String autoReplyRule;
    private Instant readAt;
    private Instant completedAt;
    private Instant transferredAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isFromCustomer() { return fromCustomer; }
    public void setFromCustomer(boolean fromCustomer) { this.fromCustomer = fromCustomer; }
    public String getHandlerId() { return handlerId; }
    public void setHandlerId(String handlerId) { this.handlerId = handlerId; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getAutoReplyRule() { return autoReplyRule; }
    public void setAutoReplyRule(String autoReplyRule) { this.autoReplyRule = autoReplyRule; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getTransferredAt() { return transferredAt; }
    public void setTransferredAt(Instant transferredAt) { this.transferredAt = transferredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static CustomerServiceMessageDTO from(CustomerServiceMessage message) {
        CustomerServiceMessageDTO dto = new CustomerServiceMessageDTO();
        dto.setId(message.getId());
        dto.setOpenid(message.getOpenid());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType() != null ? message.getMessageType().name() : null);
        dto.setStatus(message.getStatus() != null ? message.getStatus().name() : null);
        dto.setFromCustomer(message.isFromCustomer());
        dto.setHandlerId(message.getHandlerId());
        dto.setReply(message.getReply());
        dto.setAutoReplyRule(message.getAutoReplyRule());
        dto.setReadAt(message.getReadAt());
        dto.setCompletedAt(message.getCompletedAt());
        dto.setTransferredAt(message.getTransferredAt());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        return dto;
    }
}