package com.minimall.service;

import com.minimall.dto.LikeResponse;
import com.minimall.dto.LiveCommentResponse;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        liveService = new LiveService(liveRoomRepository, liveGoodsRepository,
            liveCommentRepository, liveLikeRepository);
    }

    @Test
    @DisplayName("getLiveRooms returns active rooms")
    void getLiveRooms_success() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Test Room");
        room.setLikeCount(0);

        when(liveRoomRepository.findActiveRooms()).thenReturn(List.of(room));

        var result = liveService.getLiveRooms();

        assertEquals(1, result.size());
        assertEquals("room-1", result.get(0).id());
    }

    @Test
    @DisplayName("getLiveRoom throws when not found")
    void getLiveRoom_notFound() {
        when(liveRoomRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> liveService.getLiveRoom("invalid"));
    }

    @Test
    @DisplayName("toggleLike adds like when not exists")
    void toggleLike_addLike() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(10);

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1"))
            .thenReturn(Optional.empty());
        when(liveLikeRepository.save(any(LiveLike.class))).thenAnswer(i -> i.getArgument(0));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenAnswer(i -> i.getArgument(0));

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertTrue(result.liked());
        assertEquals(11, result.likeCount());
    }

    @Test
    @DisplayName("toggleLike removes like when exists")
    void toggleLike_removeLike() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(10);

        LiveLike existingLike = new LiveLike();
        existingLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1"))
            .thenReturn(Optional.of(existingLike));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenAnswer(i -> i.getArgument(0));

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertFalse(result.liked());
        assertEquals(9, result.likeCount());
        verify(liveLikeRepository).delete(existingLike);
    }

    @Test
    @DisplayName("toggleLike throws when room not found")
    void toggleLike_roomNotFound() {
        when(liveRoomRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> liveService.toggleLike("invalid", "user-1"));
    }

    @Test
    @DisplayName("addComment creates comment")
    void addComment_success() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");

        LiveComment saved = new LiveComment();
        saved.setId("comment-1");
        saved.setLiveRoomId("room-1");
        saved.setUserId("user-1");
        saved.setUserNickname("Test");
        saved.setUserAvatar("avatar.png");
        saved.setContent("Hello");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveCommentRepository.save(any(LiveComment.class))).thenReturn(saved);

        LiveCommentResponse result = liveService.addComment("room-1", "user-1", "Test", "avatar.png", "Hello");

        assertEquals("comment-1", result.id());
        assertEquals("Hello", result.content());
    }

    @Test
    @DisplayName("addComment throws when room not found")
    void addComment_roomNotFound() {
        when(liveRoomRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> liveService.addComment("invalid", "user-1", "Test", "avatar.png", "Hello"));
    }
}