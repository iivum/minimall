package com.minimall.controller;

import com.minimall.config.JwtAuthenticationFilter.UserPrincipal;
import com.minimall.dto.*;
import com.minimall.service.LiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lives")
@Tag(name = "Live", description = "Live streaming management APIs")
public class LiveController {
    private final LiveService liveService;

    public LiveController(LiveService liveService) {
        this.liveService = liveService;
    }

    @GetMapping
    @Operation(summary = "Get all live rooms (paginated)")
    public ResponseEntity<Page<LiveRoomResponse>> getLiveRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(liveService.getLiveRooms(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all live rooms (non-paginated)")
    public ResponseEntity<List<LiveRoomResponse>> getLiveRoomsAll() {
        return ResponseEntity.ok(liveService.getLiveRooms());
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get live room details")
    public ResponseEntity<LiveRoomResponse> getLiveRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(liveService.getLiveRoom(roomId));
    }

    @GetMapping("/{roomId}/goods")
    @Operation(summary = "Get goods in a live room")
    public ResponseEntity<List<LiveGoodsResponse>> getLiveGoods(@PathVariable String roomId) {
        return ResponseEntity.ok(liveService.getLiveGoods(roomId));
    }

    @PostMapping("/{roomId}/like")
    @Operation(summary = "Toggle like on a live room")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(liveService.toggleLike(roomId, principal.userId()));
    }

    @GetMapping("/{roomId}/comments")
    @Operation(summary = "Get comments for a live room")
    public ResponseEntity<List<LiveCommentResponse>> getComments(@PathVariable String roomId) {
        return ResponseEntity.ok(liveService.getComments(roomId));
    }
}