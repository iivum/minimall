package com.minimall.dto;

import com.minimall.model.LiveComment;
import java.time.Instant;

public record LiveCommentResponse(
    String id,
    String liveRoomId,
    String userId,
    String userNickname,
    String userAvatar,
    String content,
    Instant createdAt
) {
    public static LiveCommentResponse from(LiveComment comment) {
        return new LiveCommentResponse(
            comment.getId(),
            comment.getLiveRoomId(),
            comment.getUserId(),
            comment.getUserNickname(),
            comment.getUserAvatar(),
            comment.getContent(),
            comment.getCreatedAt()
        );
    }
}