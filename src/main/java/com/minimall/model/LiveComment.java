package com.minimall.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "live_comments", indexes = {
    @Index(name = "idx_live_comments_room", columnList = "live_room_id")
})
public class LiveComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "live_room_id", nullable = false)
    private String liveRoomId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @Column(name = "user_avatar")
    private String userAvatar;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLiveRoomId() { return liveRoomId; }
    public void setLiveRoomId(String liveRoomId) { this.liveRoomId = liveRoomId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
}