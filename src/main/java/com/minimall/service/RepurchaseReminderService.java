package com.minimall.service;

import com.minimall.dto.ReminderResponse;
import com.minimall.model.RepurchaseReminder;
import com.minimall.model.RepurchaseReminder.ReminderStatus;
import com.minimall.model.RepurchaseReminder.ReminderType;
import com.minimall.model.User;
import com.minimall.repository.RepurchaseReminderRepository;
import com.minimall.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class RepurchaseReminderService {
    private final RepurchaseReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final RepurchaseCycleAnalysisService cycleAnalysisService;

    public RepurchaseReminderService(
            RepurchaseReminderRepository reminderRepository,
            UserRepository userRepository,
            RepurchaseCycleAnalysisService cycleAnalysisService) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.cycleAnalysisService = cycleAnalysisService;
    }

    @Transactional
    public ReminderResponse createPurchaseCompleteReminder(String userId, String orderId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        RepurchaseReminder reminder = new RepurchaseReminder();
        reminder.setUser(user);
        reminder.setOrderId(orderId);
        reminder.setReminderType(ReminderType.PURCHASE_COMPLETE);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setMaxSendCount(3);
        reminder.setSendCount(0);

        Instant nextTime = cycleAnalysisService.calculateNextOptimalReminderTime(userId);
        reminder.setNextSendTime(nextTime);

        RepurchaseReminder saved = reminderRepository.save(reminder);
        return toResponse(saved);
    }

    public List<ReminderResponse> getUserReminders(String userId) {
        return reminderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ReminderResponse cancelReminder(String reminderId) {
        RepurchaseReminder reminder = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new IllegalArgumentException("Reminder not found: " + reminderId));

        reminder.setStatus(ReminderStatus.CANCELLED);
        return toResponse(reminderRepository.save(reminder));
    }

    public List<RepurchaseReminder> getPendingReminders() {
        return reminderRepository.findPendingRemindersToSend(ReminderStatus.PENDING, Instant.now());
    }

    @Transactional
    public void markAsSent(String reminderId, String templateId) {
        RepurchaseReminder reminder = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new IllegalArgumentException("Reminder not found: " + reminderId));

        reminder.setSent(true);
        reminder.setSentAt(Instant.now());
        reminder.setStatus(ReminderStatus.SENT);
        reminder.setLastSentTemplateId(templateId);
        reminder.setSendCount(reminder.getSendCount() + 1);

        reminderRepository.save(reminder);
    }

    @Transactional
    public void resetForRetry(String reminderId) {
        RepurchaseReminder reminder = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new IllegalArgumentException("Reminder not found: " + reminderId));

        reminder.setSent(false);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setNextSendTime(Instant.now().plusSeconds(86400));

        reminderRepository.save(reminder);
    }

    private ReminderResponse toResponse(RepurchaseReminder reminder) {
        return new ReminderResponse(
            reminder.getId(),
            reminder.getReminderType().name(),
            reminder.getStatus().name(),
            reminder.getUser().getId(),
            reminder.getOrderId(),
            reminder.getSendCount(),
            reminder.getMaxSendCount(),
            reminder.getNextSendTime(),
            reminder.getSentAt()
        );
    }
}