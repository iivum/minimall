package com.minimall.repository;

import com.minimall.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, String> {

    List<AnalyticsEvent> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AnalyticsEvent> findByEventTypeOrderByCreatedAtDesc(String eventType);

    List<AnalyticsEvent> findByUserIdAndEventTypeOrderByCreatedAtDesc(String userId, String eventType);

    @Query("SELECT e FROM AnalyticsEvent e WHERE e.createdAt >= :start AND e.createdAt < :end ORDER BY e.createdAt DESC")
    List<AnalyticsEvent> findByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT e.eventType, COUNT(e) FROM AnalyticsEvent e WHERE e.createdAt >= :start AND e.createdAt < :end GROUP BY e.eventType")
    List<Object[]> countEventsByType(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT e.userId, COUNT(e) FROM AnalyticsEvent e WHERE e.createdAt >= :start AND e.createdAt < :end GROUP BY e.userId ORDER BY COUNT(e) DESC")
    List<Object[]> countActiveUsersByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DATE(e.createdAt), COUNT(e) FROM AnalyticsEvent e WHERE e.createdAt >= :start AND e.createdAt < :end GROUP BY DATE(e.createdAt) ORDER BY DATE(e.createdAt)")
    List<Object[]> dailyEventCounts(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT e.targetType, COUNT(e) FROM AnalyticsEvent e WHERE e.targetType IS NOT NULL AND e.createdAt >= :start AND e.createdAt < :end GROUP BY e.targetType")
    List<Object[]> countEventsByTargetType(@Param("start") Instant start, @Param("end") Instant end);
}
