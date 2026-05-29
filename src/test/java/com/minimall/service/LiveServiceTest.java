package com.minimall.service;

import com.minimall.dto.LikeResponse;
import com.minimall.dto.LiveCommentResponse;
import com.minimall.dto.LiveGoodsResponse;
import com.minimall.dto.LiveRoomResponse;
import com.minimall.model.LiveComment;
import com.minimall.model.LiveGoods;
import com.minimall.model.LiveLike;
import com.minimall.model.LiveRoom;
import com.minimall.repository.LiveCommentRepository;
import com.minimall.repository.LiveGoodsRepository;
import com.minimall.repository.LiveLikeRepository;
import com.minimall.repository.LiveRoomRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock private LiveRoomRepository liveRoomRepository;
    @Mock private LiveGoodsRepository liveGoodsRepository;
    @Mock private LiveCommentRepository liveCommentRepository;
    @Mock private LiveLikeRepository liveLikeRepository;

    private LiveService liveService;

    @BeforeEach
    void setUp() {
        liveService = new LiveService(liveRoomRepository, liveGoodsRepository, liveCommentRepository, liveLikeRepository);
    }

    @Test
    void getLiveRooms_returnsActiveRooms() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Live Stream");
        room.setStatus(LiveRoom.LiveStatus.LIVE);

        when(liveRoomRepository.findActiveRooms()).thenReturn(List.of(room));

        List<LiveRoomResponse> result = liveService.getLiveRooms();

        assertEquals(1, result.size());
        assertEquals("room-1", result.get(0).id());
    }

    @Test
    void getLiveRooms_withPagination_returnsPagedRooms() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Live Stream");

        Page<LiveRoom> page = new PageImpl<>(List.of(room));
        when(liveRoomRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<LiveRoomResponse> result = liveService.getLiveRooms(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getLiveRoom_returnsRoomWhenFound() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Live Stream");
        room.setStatus(LiveRoom.LiveStatus.LIVE);

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));

        LiveRoomResponse result = liveService.getLiveRoom("room-1");

        assertEquals("room-1", result.id());
        assertEquals("LIVE", result.status());
    }

    @Test
    void getLiveRoom_throwsWhenNotFound() {
        when(liveRoomRepository.findById("invalid-room")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> liveService.getLiveRoom("invalid-room"));
    }

    @Test
    void getLiveGoods_returnsGoodsForRoom() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");

        LiveGoods goods = new LiveGoods();
        goods.setId("goods-1");
        goods.setLiveRoom(room);
        goods.setName("Test Product");

        when(liveGoodsRepository.findByLiveRoomIdOrderBySortOrderAsc("room-1")).thenReturn(List.of(goods));

        List<LiveGoodsResponse> result = liveService.getLiveGoods("room-1");

        assertEquals(1, result.size());
        assertEquals("goods-1", result.get(0).id());
    }

    @Test
    void toggleLike_addsLikeWhenNotExists() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(10);

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.empty());
        when(liveLikeRepository.save(any(LiveLike.class))).thenReturn(new LiveLike());
        when(liveRoomRepository.save(any(LiveRoom.class))).thenReturn(room);

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertTrue(result.liked());
        assertEquals(11, result.likeCount());
    }

    @Test
    void toggleLike_removesLikeWhenExists() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(10);

        LiveLike existingLike = new LiveLike();
        existingLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(existingLike));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenReturn(room);

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertFalse(result.liked());
        assertEquals(9, result.likeCount());
        verify(liveLikeRepository).delete(existingLike);
    }

    @Test
    void toggleLike_throwsWhenRoomNotFound() {
        when(liveRoomRepository.findById("invalid-room")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            liveService.toggleLike("invalid-room", "user-1")
        );
    }

    @Test
    void getComments_returnsCommentsForRoom() {
        LiveComment comment = new LiveComment();
        comment.setId("comment-1");
        comment.setLiveRoomId("room-1");
        comment.setContent("Great stream!");

        when(liveCommentRepository.findByLiveRoomIdOrderByCreatedAtDesc("room-1")).thenReturn(List.of(comment));

        List<LiveCommentResponse> result = liveService.getComments("room-1");

        assertEquals(1, result.size());
        assertEquals("comment-1", result.get(0).id());
    }

    @Test
    void addComment_success() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");

        LiveComment savedComment = new LiveComment();
        savedComment.setId("comment-1");
        savedComment.setLiveRoomId("room-1");
        savedComment.setContent("Test comment");
        savedComment.setUserNickname("User1");
        savedComment.setUserAvatar("avatar-url");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveCommentRepository.save(any(LiveComment.class))).thenReturn(savedComment);

        LiveCommentResponse result = liveService.addComment("room-1", "user-1", "User1", "avatar-url", "Test comment");

        assertNotNull(result);
        assertEquals("comment-1", result.id());
    }

    @Test
    void addComment_throwsWhenRoomNotFound() {
        when(liveRoomRepository.findById("invalid-room")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            liveService.addComment("invalid-room", "user-1", "User1", "avatar", "Test comment")
        );
    }
}