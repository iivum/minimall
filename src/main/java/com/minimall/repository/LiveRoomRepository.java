package com.minimall.repository;

import com.minimall.model.LiveRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LiveRoomRepository extends JpaRepository<LiveRoom, String> {

    @Query("SELECT r FROM LiveRoom r WHERE r.status = 'LIVE' OR r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<LiveRoom> findActiveRooms();

    @Query("SELECT r FROM LiveRoom r WHERE r.status = 'LIVE' ORDER BY r.viewerCount DESC")
    List<LiveRoom> findLiveRooms();

    Page<LiveRoom> findByStatusIn(List<String> statuses, Pageable pageable);
}