package com.minimall.controller;

import com.minimall.service.CustomerServiceService;
import com.minimall.model.CustomerServiceMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerServiceController.class)
class CustomerServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerServiceService customerService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    private CustomerServiceMessage createMessage(String id, String openid, String content,
            CustomerServiceMessage.MessageType type, CustomerServiceMessage.Status status) {
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setId(id);
        message.setOpenid(openid);
        message.setContent(content);
        message.setMessageType(type);
        message.setStatus(status);
        return message;
    }

    @Test
    @WithMockUser
    void receiveMessage_returnsCreatedMessage() throws Exception {
        when(customerService.receiveMessage(eq("openid-123"), eq("Hello"), any()))
            .thenReturn(createMessage("msg-1", "openid-123", "Hello",
                CustomerServiceMessage.MessageType.TEXT, CustomerServiceMessage.Status.PENDING));

        mockMvc.perform(post("/api/customer-service/receive")
                .with(csrf())
                .param("openid", "openid-123")
                .param("content", "Hello"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserMessages_returnsMessagesForOpenid() throws Exception {
        when(customerService.getUserMessages("openid-123")).thenReturn(List.of(
            createMessage("msg-1", "openid-123", "Hello",
                CustomerServiceMessage.MessageType.TEXT, CustomerServiceMessage.Status.PENDING)
        ));

        mockMvc.perform(get("/api/customer-service/messages/openid-123"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPendingMessages_returnsAllPendingMessages() throws Exception {
        when(customerService.getPendingMessages()).thenReturn(List.of(
            createMessage("msg-1", "openid-123", "Hello",
                CustomerServiceMessage.MessageType.TEXT, CustomerServiceMessage.Status.PENDING)
        ));

        mockMvc.perform(get("/api/customer-service/pending"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void markAsRead_returnsUpdatedMessage() throws Exception {
        when(customerService.markAsRead("msg-1")).thenReturn(
            createMessage("msg-1", "openid-123", "Hello",
                CustomerServiceMessage.MessageType.TEXT, CustomerServiceMessage.Status.PROCESSING));

        mockMvc.perform(post("/api/customer-service/msg-1/read")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void completeMessage_returnsCompletedMessage() throws Exception {
        when(customerService.completeMessage("msg-1")).thenReturn(
            createMessage("msg-1", "openid-123", "Hello",
                CustomerServiceMessage.MessageType.TEXT, CustomerServiceMessage.Status.COMPLETED));

        mockMvc.perform(post("/api/customer-service/msg-1/complete")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void transferToHuman_returnsTransferredMessage() throws Exception {
        CustomerServiceMessage message = createMessage("msg-1", "openid-123", "Hello",
            CustomerServiceMessage.MessageType.TEXT, CustomerServiceMessage.Status.TRANSFERRED);
        message.setHandlerId("human-handler-1");
        when(customerService.transferToHuman("msg-1", "human-handler-1")).thenReturn(message);

        mockMvc.perform(post("/api/customer-service/msg-1/transfer")
                .with(csrf())
                .param("handlerId", "human-handler-1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPendingCount_returnsCountOfPendingMessages() throws Exception {
        when(customerService.getPendingCount()).thenReturn(5L);

        mockMvc.perform(get("/api/customer-service/stats/pending-count"))
            .andExpect(status().isOk());
    }
}