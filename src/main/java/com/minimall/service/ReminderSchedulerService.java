package com.minimall.service;

import com.minimall.model.RepurchaseReminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReminderSchedulerService {
    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final RepurchaseReminderService reminderService;

    public ReminderSchedulerService(RepurchaseReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scanAndSendReminders() {
        log.info("Starting reminder scan...");

        List<RepurchaseReminder> pendingReminders = reminderService.getPendingReminders();
        log.info("Found {} pending reminders", pendingReminders.size());

        int sentCount = 0;
        for (RepurchaseReminder reminder : pendingReminders) {
            try {
                boolean success = sendReminder(reminder);
                if (success) {
                    sentCount++;
                }
            } catch (Exception e) {
                log.error("Failed to send reminder {}: {}", reminder.getId(), e.getMessage());
            }
        }

        log.info("Reminder scan completed. Sent: {}", sentCount);
    }

    private boolean sendReminder(RepurchaseReminder reminder) {
        log.info("Sending reminder {} to user {} via template message",
            reminder.getId(), reminder.getUser().getId());

        reminderService.markAsSent(reminder.getId(), "TEMPLATE_ID_PLACEHOLDER");

        return true;
    }

    public int scanAndSendRemindersManual() {
        List<RepurchaseReminder> pendingReminders = reminderService.getPendingReminders();
        int sentCount = 0;
        for (RepurchaseReminder reminder : pendingReminders) {
            try {
                if (sendReminder(reminder)) {
                    sentCount++;
                }
            } catch (Exception e) {
                log.error("Failed to send reminder {}: {}", reminder.getId(), e.getMessage());
            }
        }
        return sentCount;
    }
}