package com.minimall.repository;

import com.minimall.model.LiveComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LiveCommentRepository extends JpaRepository<LiveComment, String> {
    List<LiveComment> findByLiveRoomIdOrderByCreatedAtDesc(String liveRoomId);
}