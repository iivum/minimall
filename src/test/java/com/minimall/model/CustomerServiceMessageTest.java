package com.minimall.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceMessageTest {

    @Test
    void newMessage_hasPendingStatus() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        assertEquals(CustomerServiceMessage.Status.PENDING, msg.getStatus());
        assertFalse(msg.isFromCustomer());
    }

    @Test
    void markAsRead_updatesStatusToProcessing() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.markAsRead();
        assertEquals(CustomerServiceMessage.Status.PROCESSING, msg.getStatus());
    }

    @Test
    void complete_updatesStatusToCompleted() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.markAsRead();
        msg.complete();
        assertEquals(CustomerServiceMessage.Status.COMPLETED, msg.getStatus());
    }

    @Test
    void transferToHuman_updatesStatusAndSetsHandler() {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.transferToHuman("handler-123");
        assertEquals(CustomerServiceMessage.Status.TRANSFERRED, msg.getStatus());
        assertEquals("handler-123", msg.getHandlerId());
    }
}