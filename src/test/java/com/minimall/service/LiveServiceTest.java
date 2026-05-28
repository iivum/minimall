package com.minimall.service;

import com.minimall.dto.LikeResponse;
import com.minimall.dto.LiveCommentResponse;
import com.minimall.dto.LiveGoodsResponse;
import com.minimall.dto.LiveRoomResponse;
import com.minimall.model.LiveComment;
import com.minimall.model.LiveLike;
import com.minimall.model.LiveRoom;
import com.minimall.repository.LiveCommentRepository;
import com.minimall.repository.LiveGoodsRepository;
import com.minimall.repository.LiveLikeRepository;
import com.minimall.repository.LiveRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveServiceTest {

    @Mock
    private LiveRoomRepository liveRoomRepository;
    @Mock
    private LiveGoodsRepository liveGoodsRepository;
    @Mock
    private LiveCommentRepository liveCommentRepository;
    @Mock
    private LiveLikeRepository liveLikeRepository;

    private LiveService liveService;

    @BeforeEach
    void setUp() {
        liveService = new LiveService(liveRoomRepository, liveGoodsRepository, liveCommentRepository, liveLikeRepository);
    }

    @Test
    @DisplayName("getLiveRooms returns active rooms")
    void getLiveRooms_returnsActiveRooms() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Test Room");
        room.setStatus(LiveRoom.LiveStatus.LIVE);

        when(liveRoomRepository.findActiveRooms()).thenReturn(List.of(room));

        List<LiveRoomResponse> result = liveService.getLiveRooms();

        assertEquals(1, result.size());
        assertEquals("room-1", result.get(0).id());
    }

    @Test
    @DisplayName("getLiveRooms with pageable returns paginated rooms")
    void getLiveRooms_paginated_returnsPage() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Test Room");
        room.setStatus(LiveRoom.LiveStatus.LIVE);

        Page<LiveRoom> page = new PageImpl<>(List.of(room));
        when(liveRoomRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<LiveRoomResponse> result = liveService.getLiveRooms(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("getLiveRoom returns room when exists")
    void getLiveRoom_exists_returnsRoom() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Test Room");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));

        LiveRoomResponse result = liveService.getLiveRoom("room-1");

        assertNotNull(result);
        assertEquals("room-1", result.id());
    }

    @Test
    @DisplayName("getLiveRoom throws when room not found")
    void getLiveRoom_notFound_throws() {
        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> liveService.getLiveRoom("room-1"));
    }

    @Test
    @DisplayName("getLiveGoods returns goods for room")
    void getLiveGoods_returnsGoods() {
        com.minimall.model.LiveGoods goods = new com.minimall.model.LiveGoods();
        goods.setId("goods-1");
        goods.setName("Test Product");
        goods.setPrice(java.math.BigDecimal.valueOf(99.99));

        when(liveGoodsRepository.findByLiveRoomIdOrderBySortOrderAsc("room-1")).thenReturn(List.of(goods));

        List<LiveGoodsResponse> result = liveService.getLiveGoods("room-1");

        assertEquals(1, result.size());
        assertEquals("goods-1", result.get(0).id());
    }

    @Test
    @DisplayName("toggleLike adds like when no existing like")
    void toggleLike_addsLike() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(10);

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.empty());
        when(liveLikeRepository.save(any(LiveLike.class))).thenAnswer(i -> i.getArgument(0));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenAnswer(i -> i.getArgument(0));

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertTrue(result.liked());
        assertEquals(11, result.likeCount());
    }

    @Test
    @DisplayName("toggleLike removes like when existing like")
    void toggleLike_removesLike() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(10);

        LiveLike existingLike = new LiveLike();
        existingLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(existingLike));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenAnswer(i -> i.getArgument(0));

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertFalse(result.liked());
        assertEquals(9, result.likeCount());
        verify(liveLikeRepository).delete(existingLike);
    }

    @Test
    @DisplayName("toggleLike does not go below zero")
    void toggleLike_doesNotGoBelowZero() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(0);

        LiveLike existingLike = new LiveLike();
        existingLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(existingLike));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenAnswer(i -> i.getArgument(0));

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertEquals(0, result.likeCount());
    }

    @Test
    @DisplayName("getComments returns comments for room")
    void getComments_returnsComments() {
        LiveComment comment = new LiveComment();
        comment.setId("comment-1");
        comment.setContent("Test comment");

        when(liveCommentRepository.findByLiveRoomIdOrderByCreatedAtDesc("room-1")).thenReturn(List.of(comment));

        List<LiveCommentResponse> result = liveService.getComments("room-1");

        assertEquals(1, result.size());
        assertEquals("comment-1", result.get(0).id());
    }

    @Test
    @DisplayName("addComment adds comment successfully")
    void addComment_success() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");

        LiveComment savedComment = new LiveComment();
        savedComment.setId("comment-1");
        savedComment.setContent("New comment");
        savedComment.setUserNickname("Test User");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveCommentRepository.save(any(LiveComment.class))).thenReturn(savedComment);

        LiveCommentResponse result = liveService.addComment("room-1", "user-1", "Test User", "avatar-url", "New comment");

        assertNotNull(result);
        assertEquals("comment-1", result.id());
        assertEquals("New comment", result.content());
    }

    @Test
    @DisplayName("addComment throws when room not found")
    void addComment_roomNotFound_throws() {
        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            liveService.addComment("room-1", "user-1", "Test User", "avatar-url", "New comment"));
    }
}
