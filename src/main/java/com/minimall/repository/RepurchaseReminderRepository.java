package com.minimall.repository;

import com.minimall.model.RepurchaseReminder;
import com.minimall.model.RepurchaseReminder.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface RepurchaseReminderRepository extends JpaRepository<RepurchaseReminder, String> {

    List<RepurchaseReminder> findByUserIdAndStatus(String userId, ReminderStatus status);

    List<RepurchaseReminder> findByStatusAndNextSendTimeBefore(ReminderStatus status, Instant time);

    @Query("SELECT r FROM RepurchaseReminder r WHERE r.status = :status AND r.nextSendTime IS NOT NULL AND r.nextSendTime <= :time AND r.sendCount < r.maxSendCount")
    List<RepurchaseReminder> findPendingRemindersToSend(@Param("status") ReminderStatus status, @Param("time") Instant time);

    List<RepurchaseReminder> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndStatus(String userId, ReminderStatus status);
}