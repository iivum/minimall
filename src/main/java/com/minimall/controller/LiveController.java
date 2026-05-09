package com.minimall.controller;

import com.minimall.config.JwtAuthenticationFilter.UserPrincipal;
import com.minimall.dto.*;
import com.minimall.service.LiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "Get all live rooms")
    public ResponseEntity<List<LiveRoomResponse>> getLiveRooms() {
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