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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void getLiveRooms_returnsActiveRooms() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Test Room");

        when(liveRoomRepository.findActiveRooms()).thenReturn(List.of(room));

        var result = liveService.getLiveRooms();

        assertEquals(1, result.size());
        assertEquals("room-1", result.get(0).id());
    }

    @Test
    void getLiveRoom_returnsRoomWhenExists() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setTitle("Test Room");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));

        var result = liveService.getLiveRoom("room-1");

        assertEquals("room-1", result.id());
    }

    @Test
    void getLiveRoom_throwsWhenNotFound() {
        when(liveRoomRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> liveService.getLiveRoom("unknown"));
    }

    @Test
    void toggleLike_addsLikeWhenNotExists() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(0);

        LiveLike newLike = new LiveLike();
        newLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.empty());
        when(liveLikeRepository.save(any(LiveLike.class))).thenReturn(newLike);
        when(liveRoomRepository.save(any(LiveRoom.class))).thenReturn(room);

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertTrue(result.liked());
        assertEquals(1, result.likeCount());
    }

    @Test
    void toggleLike_removesLikeWhenExists() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(5);

        LiveLike existingLike = new LiveLike();
        existingLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(existingLike));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenReturn(room);

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertFalse(result.liked());
        assertEquals(4, result.likeCount());
        verify(liveLikeRepository).delete(existingLike);
    }

    @Test
    void toggleLike_preventsNegativeCount() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");
        room.setLikeCount(0);

        LiveLike existingLike = new LiveLike();
        existingLike.setId("like-1");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveLikeRepository.findByLiveRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(existingLike));
        when(liveRoomRepository.save(any(LiveRoom.class))).thenReturn(room);

        LikeResponse result = liveService.toggleLike("room-1", "user-1");

        assertFalse(result.liked());
        assertEquals(0, result.likeCount());
    }

    @Test
    void addComment_savesCommentSuccessfully() {
        LiveRoom room = new LiveRoom();
        room.setId("room-1");

        LiveComment savedComment = new LiveComment();
        savedComment.setId("comment-1");
        savedComment.setContent("Test comment");

        when(liveRoomRepository.findById("room-1")).thenReturn(Optional.of(room));
        when(liveCommentRepository.save(any(LiveComment.class))).thenReturn(savedComment);

        LiveCommentResponse result = liveService.addComment("room-1", "user-1", "User Nick", "avatar.png", "Test comment");

        assertNotNull(result);
        verify(liveCommentRepository).save(any(LiveComment.class));
    }
}