package com.minimall.dto;

import com.minimall.model.LiveRoom;
import java.time.Instant;
import java.util.List;

public record LiveRoomResponse(
    String id,
    String title,
    String coverUrl,
    String streamUrl,
    String anchorName,
    String anchorAvatar,
    Integer viewerCount,
    Integer likeCount,
    Integer goodsCount,
    String status,
    Instant startTime,
    Instant endTime,
    List<LiveGoodsResponse> goods
) {
    public static LiveRoomResponse from(LiveRoom room) {
        return new LiveRoomResponse(
            room.getId(),
            room.getTitle(),
            room.getCoverUrl(),
            room.getStreamUrl(),
            room.getAnchorName(),
            room.getAnchorAvatar(),
            room.getViewerCount(),
            room.getLikeCount(),
            room.getGoodsCount(),
            room.getStatus().name(),
            room.getStartTime(),
            room.getEndTime(),
            null
        );
    }
}