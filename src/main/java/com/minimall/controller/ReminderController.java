package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.ReminderResponse;
import com.minimall.service.RepurchaseReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    private final RepurchaseReminderService reminderService;
    private final SecurityUtils securityUtils;

    public ReminderController(RepurchaseReminderService reminderService, SecurityUtils securityUtils) {
        this.reminderService = reminderService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReminderResponse>> getMyReminders() {
        String userId = securityUtils.getCurrentUserId();
        List<ReminderResponse> reminders = reminderService.getUserReminders(userId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReminderResponse>> getUserReminders(@PathVariable String userId) {
        List<ReminderResponse> reminders = reminderService.getUserReminders(userId);
        return ResponseEntity.ok(reminders);
    }

    @PostMapping("/{reminderId}/cancel")
    public ResponseEntity<ReminderResponse> cancelReminder(@PathVariable String reminderId) {
        ReminderResponse response = reminderService.cancelReminder(reminderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scan")
    public ResponseEntity<String> triggerScan() {
        return ResponseEntity.ok("Scan triggered");
    }
}