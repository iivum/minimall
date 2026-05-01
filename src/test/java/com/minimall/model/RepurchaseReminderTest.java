package com.minimall.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class RepurchaseReminderTest {

    @Test
    void constructor_setsFieldsCorrectly() {
        RepurchaseReminder.ReminderType type = RepurchaseReminder.ReminderType.PURCHASE_COMPLETE;
        RepurchaseReminder.ReminderStatus status = RepurchaseReminder.ReminderStatus.PENDING;

        RepurchaseReminder reminder = new RepurchaseReminder(type, status);

        assertEquals(type, reminder.getReminderType());
        assertEquals(status, reminder.getStatus());
        assertNotNull(reminder.getId());
        assertFalse(reminder.isSent());
    }

    @Test
    void canResend_whenNotSentAndWithinWindow_returnsTrue() {
        RepurchaseReminder reminder = new RepurchaseReminder(
            RepurchaseReminder.ReminderType.PURCHASE_COMPLETE,
            RepurchaseReminder.ReminderStatus.PENDING
        );
        reminder.setNextSendTime(Instant.now().plusSeconds(3600));

        assertTrue(reminder.canResend());
    }

    @Test
    void markAsSent_updatesStatusAndTimestamp() {
        RepurchaseReminder reminder = new RepurchaseReminder(
            RepurchaseReminder.ReminderType.PURCHASE_COMPLETE,
            RepurchaseReminder.ReminderStatus.PENDING
        );

        reminder.markAsSent();

        assertTrue(reminder.isSent());
        assertNotNull(reminder.getSentAt());
        assertEquals(RepurchaseReminder.ReminderStatus.SENT, reminder.getStatus());
    }
}