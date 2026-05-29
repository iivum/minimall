package com.minimall.controller;

import com.minimall.service.LiveService;
import com.minimall.dto.LiveRoomResponse;
import com.minimall.dto.LiveGoodsResponse;
import com.minimall.dto.LiveCommentResponse;
import com.minimall.dto.LikeResponse;
import com.minimall.config.JwtAuthenticationFilter.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LiveController.class)
class LiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LiveService liveService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    @Test
    @WithMockUser
    void getLiveRooms_returnsPaginatedRooms() throws Exception {
        LiveRoomResponse room = new LiveRoomResponse(
            "room-1", "Live Stream 1", "https://example.com/cover.jpg", "https://example.com/stream",
            "主播1", "https://example.com/avatar.jpg", 100, 50, 10,
            "LIVE", Instant.now(), Instant.now(), null
        );
        Page<LiveRoomResponse> page = new PageImpl<>(List.of(room));
        when(liveService.getLiveRooms(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/lives")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getLiveRoomsAll_returnsAllRooms() throws Exception {
        LiveRoomResponse room = new LiveRoomResponse(
            "room-1", "Live Stream 1", "https://example.com/cover.jpg", "https://example.com/stream",
            "主播1", "https://example.com/avatar.jpg", 100, 50, 10,
            "LIVE", Instant.now(), Instant.now(), null
        );
        when(liveService.getLiveRooms()).thenReturn(List.of(room));

        mockMvc.perform(get("/api/lives/all"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getLiveRoom_returnsRoomDetails() throws Exception {
        LiveRoomResponse room = new LiveRoomResponse(
            "room-1", "Live Stream 1", "https://example.com/cover.jpg", "https://example.com/stream",
            "主播1", "https://example.com/avatar.jpg", 100, 50, 10,
            "LIVE", Instant.now(), Instant.now(), null
        );
        when(liveService.getLiveRoom("room-1")).thenReturn(room);

        mockMvc.perform(get("/api/lives/room-1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getLiveGoods_returnsGoodsInRoom() throws Exception {
        LiveGoodsResponse goods = new LiveGoodsResponse(
            "goods-1", "prod-1", "Product Name", "https://example.com/img.jpg",
            BigDecimal.valueOf(99.99), BigDecimal.valueOf(129.99), 100, 50, 1
        );
        when(liveService.getLiveGoods("room-1")).thenReturn(List.of(goods));

        mockMvc.perform(get("/api/lives/room-1/goods"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getComments_returnsCommentsForRoom() throws Exception {
        LiveCommentResponse comment = new LiveCommentResponse(
            "comment-1", "room-1", "user-1", "Nick", "https://example.com/avatar.jpg",
            "Comment text", Instant.now()
        );
        when(liveService.getComments("room-1")).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/lives/room-1/comments"))
            .andExpect(status().isOk());
    }
}