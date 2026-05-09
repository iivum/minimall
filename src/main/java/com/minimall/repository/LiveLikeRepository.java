package com.minimall.repository;

import com.minimall.model.LiveLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface LiveLikeRepository extends JpaRepository<LiveLike, String> {
    Optional<LiveLike> findByLiveRoomIdAndUserId(String liveRoomId, String userId);

    @Modifying
    @Query("DELETE FROM LiveLike l WHERE l.liveRoomId = :liveRoomId AND l.userId = :userId")
    void deleteByLiveRoomIdAndUserId(String liveRoomId, String userId);
}