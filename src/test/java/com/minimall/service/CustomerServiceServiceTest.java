package com.minimall.service;

import com.minimall.model.CustomerServiceMessage;
import com.minimall.repository.CustomerServiceMessageRepository;
import com.minimall.config.CustomerServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceServiceTest {

    @Mock
    private CustomerServiceMessageRepository repository;

    private CustomerServiceConfig config;
    private CustomerServiceService service;

    @BeforeEach
    void setUp() {
        config = new CustomerServiceConfig();
        config.setEnabled(true);
        service = new CustomerServiceService(repository, config);
    }

    @Test
    @DisplayName("processMessage with keyword match returns auto reply")
    void processMessage_withKeywordMatch_returnsAutoReply() {
        Map<String, String> rules = new HashMap<>();
        rules.put("退货", "亲，退款请在订单详情页申请哦~");
        config.setRules(rules);

        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("我要退货");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(true);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String reply = service.processMessage(msg);

        assertNotNull(reply);
        assertEquals("亲，退款请在订单详情页申请哦~", reply);
        assertEquals("退货", msg.getAutoReplyRule());
        verify(repository, times(1)).save(msg);
    }

    @Test
    @DisplayName("processMessage with transfer keyword sets TRANSFERRED status")
    void processMessage_withTransferKeyword_setsTransferredStatus() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("我要转人工");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(true);
        msg.setStatus(CustomerServiceMessage.Status.PENDING);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String reply = service.processMessage(msg);

        assertNotNull(reply);
        assertEquals("您已转入人工客服，请稍候...", reply);
        assertEquals(CustomerServiceMessage.Status.TRANSFERRED, msg.getStatus());
        assertNotNull(msg.getHandlerId());
        assertTrue(msg.getHandlerId().startsWith("human-"));
    }

    @Test
    @DisplayName("processMessage with no match returns default reply")
    void processMessage_withNoMatch_returnsDefaultReply() {
        Map<String, String> rules = new HashMap<>();
        rules.put("退货", "亲，退款请在订单详情页申请哦~");
        config.setRules(rules);

        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("我想知道运费怎么算");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(true);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String reply = service.processMessage(msg);

        assertNotNull(reply);
        assertEquals("感谢您的留言，我们会尽快回复您~", reply);
        assertNull(msg.getAutoReplyRule());
        verify(repository, times(1)).save(msg);
    }

    @Test
    @DisplayName("receiveMessage first visit sends welcome reply")
    void receiveMessage_firstVisit_sendsWelcomeReply() {
        when(repository.findByOpenidOrderByCreatedAtDesc("new_user"))
            .thenReturn(Collections.emptyList());
        when(repository.save(any())).thenAnswer(inv -> {
            CustomerServiceMessage m = inv.getArgument(0);
            m.setId("msg-123");
            return m;
        });

        CustomerServiceMessage result = service.receiveMessage(
            "new_user", "你好", CustomerServiceMessage.MessageType.TEXT);

        assertNotNull(result);
        assertEquals("欢迎光临！有什么可以帮您的吗？", result.getReply());
        assertEquals("first_visit", result.getAutoReplyRule());
    }

    @Test
    @DisplayName("receiveMessage not first visit does not send welcome reply")
    void receiveMessage_notFirstVisit_noWelcomeReply() {
        CustomerServiceMessage existing = new CustomerServiceMessage();
        existing.setOpenid("existing_user");

        when(repository.findByOpenidOrderByCreatedAtDesc("existing_user"))
            .thenReturn(List.of(existing));
        when(repository.save(any())).thenAnswer(inv -> {
            CustomerServiceMessage m = inv.getArgument(0);
            m.setId("msg-456");
            return m;
        });

        CustomerServiceMessage result = service.receiveMessage(
            "existing_user", "继续咨询", CustomerServiceMessage.MessageType.TEXT);

        assertNotNull(result);
        assertNull(result.getReply());
        assertNull(result.getAutoReplyRule());
    }

    @Test
    @DisplayName("processMessage from system returns null")
    void processMessage_fromSystem_returnsNull() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("system message");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(false);

        String reply = service.processMessage(msg);

        assertNull(reply);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("processMessage with 人工 keyword transfers to human")
    void processMessage_with人工Keyword_transfersToHuman() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("请联系人工客服");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(true);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String reply = service.processMessage(msg);

        assertEquals("您已转入人工客服，请稍候...", reply);
        assertEquals(CustomerServiceMessage.Status.TRANSFERRED, msg.getStatus());
    }

    @Test
    @DisplayName("processMessage with 客服 keyword transfers to human")
    void processMessage_with客服Keyword_transfersToHuman() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("请问客服电话是多少");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(true);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String reply = service.processMessage(msg);

        assertEquals("您已转入人工客服，请稍候...", reply);
        assertEquals(CustomerServiceMessage.Status.TRANSFERRED, msg.getStatus());
    }

    @Test
    @DisplayName("getPendingCount returns correct count")
    void getPendingCount_returnsCorrectCount() {
        when(repository.countByStatus(CustomerServiceMessage.Status.PENDING)).thenReturn(5L);

        long count = service.getPendingCount();

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("markAsRead updates message status to PROCESSING")
    void markAsRead_updatesMessageStatusToProcessing() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setId("msg-123");
        msg.setStatus(CustomerServiceMessage.Status.PENDING);

        when(repository.findById("msg-123")).thenReturn(java.util.Optional.of(msg));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerServiceMessage result = service.markAsRead("msg-123");

        assertEquals(CustomerServiceMessage.Status.PROCESSING, result.getStatus());
        assertNotNull(result.getReadAt());
    }

    @Test
    @DisplayName("completeMessage updates message status to COMPLETED")
    void completeMessage_updatesMessageStatusToCompleted() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setId("msg-123");
        msg.setStatus(CustomerServiceMessage.Status.PROCESSING);

        when(repository.findById("msg-123")).thenReturn(java.util.Optional.of(msg));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerServiceMessage result = service.completeMessage("msg-123");

        assertEquals(CustomerServiceMessage.Status.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    @DisplayName("transferToHuman sets transferred status and handler")
    void transferToHuman_setsTransferredStatusAndHandler() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setId("msg-123");
        msg.setStatus(CustomerServiceMessage.Status.PENDING);

        when(repository.findById("msg-123")).thenReturn(java.util.Optional.of(msg));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerServiceMessage result = service.transferToHuman("msg-123", "handler-001");

        assertEquals(CustomerServiceMessage.Status.TRANSFERRED, result.getStatus());
        assertEquals("handler-001", result.getHandlerId());
        assertNotNull(result.getTransferredAt());
    }

    @Test
    @DisplayName("markAsRead throws exception when message not found")
    void markAsRead_throwsExceptionWhenMessageNotFound() {
        when(repository.findById("nonexistent")).thenReturn(java.util.Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.markAsRead("nonexistent"));
    }

    @Test
    @DisplayName("completeMessage throws exception when message not found")
    void completeMessage_throwsExceptionWhenMessageNotFound() {
        when(repository.findById("nonexistent")).thenReturn(java.util.Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.completeMessage("nonexistent"));
    }

    @Test
    @DisplayName("transferToHuman throws exception when message not found")
    void transferToHuman_throwsExceptionWhenMessageNotFound() {
        when(repository.findById("nonexistent")).thenReturn(java.util.Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.transferToHuman("nonexistent", "handler-001"));
    }

    @Test
    @DisplayName("getUserMessages returns messages in descending order")
    void getUserMessages_returnsMessagesInDescendingOrder() {
        CustomerServiceMessage msg1 = new CustomerServiceMessage();
        msg1.setContent("first message");
        CustomerServiceMessage msg2 = new CustomerServiceMessage();
        msg2.setContent("second message");

        when(repository.findByOpenidOrderByCreatedAtDesc("test_user"))
            .thenReturn(List.of(msg2, msg1));

        List<CustomerServiceMessage> result = service.getUserMessages("test_user");

        assertEquals(2, result.size());
        assertEquals("second message", result.get(0).getContent());
        assertEquals("first message", result.get(1).getContent());
    }

    @Test
    @DisplayName("getPendingMessages returns pending messages without handler")
    void getPendingMessages_returnsPendingMessagesWithoutHandler() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("pending message");

        when(repository.findByStatusAndHandlerIdIsNull(CustomerServiceMessage.Status.PENDING))
            .thenReturn(List.of(msg));

        List<CustomerServiceMessage> result = service.getPendingMessages();

        assertEquals(1, result.size());
        assertEquals("pending message", result.get(0).getContent());
    }

    @Test
    @DisplayName("processMessage when auto-reply disabled returns null")
    void processMessage_whenAutoReplyDisabled_returnsNull() {
        config.setEnabled(false);

        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setContent("我要退货");
        msg.setOpenid("test_openid");
        msg.setFromCustomer(true);

        String reply = service.processMessage(msg);

        assertNull(reply);
        verify(repository, never()).save(any());
    }
}