package com.minimall.service;

import com.minimall.dto.*;
import com.minimall.model.*;
import com.minimall.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LiveService {
    private final LiveRoomRepository liveRoomRepository;
    private final LiveGoodsRepository liveGoodsRepository;
    private final LiveCommentRepository liveCommentRepository;
    private final LiveLikeRepository liveLikeRepository;

    public LiveService(
            LiveRoomRepository liveRoomRepository,
            LiveGoodsRepository liveGoodsRepository,
            LiveCommentRepository liveCommentRepository,
            LiveLikeRepository liveLikeRepository) {
        this.liveRoomRepository = liveRoomRepository;
        this.liveGoodsRepository = liveGoodsRepository;
        this.liveCommentRepository = liveCommentRepository;
        this.liveLikeRepository = liveLikeRepository;
    }

    public List<LiveRoomResponse> getLiveRooms() {
        return liveRoomRepository.findActiveRooms().stream()
            .map(LiveRoomResponse::from)
            .toList();
    }

    public LiveRoomResponse getLiveRoom(String roomId) {
        LiveRoom room = liveRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Live room not found: " + roomId));
        return LiveRoomResponse.from(room);
    }

    public List<LiveGoodsResponse> getLiveGoods(String roomId) {
        return liveGoodsRepository.findByLiveRoomIdOrderBySortOrderAsc(roomId).stream()
            .map(LiveGoodsResponse::from)
            .toList();
    }

    @Transactional
    public LikeResponse toggleLike(String roomId, String userId) {
        LiveRoom room = liveRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Live room not found: " + roomId));

        var existingLike = liveLikeRepository.findByLiveRoomIdAndUserId(roomId, userId);

        if (existingLike.isPresent()) {
            liveLikeRepository.delete(existingLike.get());
            room.setLikeCount(Math.max(0, room.getLikeCount() - 1));
            liveRoomRepository.save(room);
            return new LikeResponse(false, room.getLikeCount());
        } else {
            LiveLike like = new LiveLike();
            like.setLiveRoomId(roomId);
            like.setUserId(userId);
            liveLikeRepository.save(like);
            room.setLikeCount(room.getLikeCount() + 1);
            liveRoomRepository.save(room);
            return new LikeResponse(true, room.getLikeCount());
        }
    }

    public List<LiveCommentResponse> getComments(String roomId) {
        return liveCommentRepository.findByLiveRoomIdOrderByCreatedAtDesc(roomId).stream()
            .map(LiveCommentResponse::from)
            .toList();
    }

    @Transactional
    public LiveCommentResponse addComment(String roomId, String userId, String nickname, String avatar, String content) {
        LiveRoom room = liveRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Live room not found: " + roomId));

        LiveComment comment = new LiveComment();
        comment.setLiveRoomId(roomId);
        comment.setUserId(userId);
        comment.setUserNickname(nickname);
        comment.setUserAvatar(avatar);
        comment.setContent(content);
        return LiveCommentResponse.from(liveCommentRepository.save(comment));
    }
}