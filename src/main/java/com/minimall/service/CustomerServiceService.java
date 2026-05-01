package com.minimall.service;

import com.minimall.model.CustomerServiceMessage;
import com.minimall.repository.CustomerServiceMessageRepository;
import com.minimall.config.CustomerServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceService {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceService.class);

    private static final String DEFAULT_REPLY = "感谢您的留言，我们会尽快回复您~";
    private static final String WELCOME_REPLY = "欢迎光临！有什么可以帮您的吗？";
    private static final String OFF_HOURS_REPLY = "现在是非工作时间，客服将在工作时间内尽快回复您~";
    private static final String TRANSFER_KEYWORD = "人工";

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    private final CustomerServiceMessageRepository repository;
    private final CustomerServiceConfig config;

    public CustomerServiceService(CustomerServiceMessageRepository repository,
                                   CustomerServiceConfig config) {
        this.repository = repository;
        this.config = config;
    }

    @Transactional
    public String processMessage(CustomerServiceMessage message) {
        if (!message.isFromCustomer()) {
            return null;
        }

        // 如果自动回复被禁用，直接返回null
        if (!config.isEnabled()) {
            return null;
        }

        String content = message.getContent();

        // 检查是否转人工
        if (shouldTransferToHuman(content)) {
            return handleTransfer(message);
        }

        // 检查自动回复规则
        String autoReply = findAutoReply(content);
        if (autoReply != null) {
            message.setReply(autoReply);
            message.setAutoReplyRule(findMatchingRule(content));
            repository.save(message);
            return autoReply;
        }

        // 默认回复
        message.setReply(DEFAULT_REPLY);
        repository.save(message);
        return DEFAULT_REPLY;
    }

    @Transactional
    public CustomerServiceMessage receiveMessage(String openid, String content,
                                                  CustomerServiceMessage.MessageType type) {
        // 检查是否首次来访
        List<CustomerServiceMessage> existing = repository.findByOpenidOrderByCreatedAtDesc(openid);
        boolean isFirstVisit = existing.isEmpty();

        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setOpenid(openid);
        message.setContent(content);
        message.setMessageType(type);
        message.setFromCustomer(true);
        message.setStatus(CustomerServiceMessage.Status.PENDING);

        CustomerServiceMessage saved = repository.save(message);

        // 首次来访自动回复欢迎语
        if (isFirstVisit && config.isEnabled()) {
            saved.setReply(WELCOME_REPLY);
            saved.setAutoReplyRule("first_visit");
            repository.save(saved);
        }

        return saved;
    }

    public List<CustomerServiceMessage> getUserMessages(String openid) {
        return repository.findByOpenidOrderByCreatedAtDesc(openid);
    }

    public List<CustomerServiceMessage> getPendingMessages() {
        return repository.findByStatusAndHandlerIdIsNull(CustomerServiceMessage.Status.PENDING);
    }

    @Transactional
    public CustomerServiceMessage markAsRead(String messageId) {
        CustomerServiceMessage message = repository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        message.markAsRead();
        return repository.save(message);
    }

    @Transactional
    public CustomerServiceMessage completeMessage(String messageId) {
        CustomerServiceMessage message = repository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        message.complete();
        return repository.save(message);
    }

    @Transactional
    public CustomerServiceMessage transferToHuman(String messageId, String handlerId) {
        CustomerServiceMessage message = repository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        message.transferToHuman(handlerId);
        return repository.save(message);
    }

    public long getPendingCount() {
        return repository.countByStatus(CustomerServiceMessage.Status.PENDING);
    }

    private boolean shouldTransferToHuman(String content) {
        return content.contains(TRANSFER_KEYWORD) ||
               content.contains("客服") ||
               content.contains("转人工");
    }

    private String handleTransfer(CustomerServiceMessage message) {
        String handlerId = "human-" + System.currentTimeMillis();
        message.setHandlerId(handlerId);
        message.setStatus(CustomerServiceMessage.Status.TRANSFERRED);
        message.setReply("您已转入人工客服，请稍候...");
        repository.save(message);
        log.info("Message {} transferred to human handler: {}", message.getId(), handlerId);
        return message.getReply();
    }

    private String findAutoReply(String content) {
        if (!config.isEnabled()) {
            return null;
        }

        for (Map.Entry<String, String> rule : config.getRules().entrySet()) {
            if (content.contains(rule.getKey())) {
                return rule.getValue();
            }
        }
        return null;
    }

    private String findMatchingRule(String content) {
        for (Map.Entry<String, String> rule : config.getRules().entrySet()) {
            if (content.contains(rule.getKey())) {
                return rule.getKey();
            }
        }
        return null;
    }

    private boolean isWithinWorkingHours() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(WORK_START) && !now.isAfter(WORK_END);
    }
}