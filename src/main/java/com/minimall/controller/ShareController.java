package com.minimall.controller;

import com.minimall.dto.ShareRequest;
import com.minimall.dto.ShareResponse;
import com.minimall.model.ShareReward;
import com.minimall.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/share")
@Tag(name = "Share", description = "Share reward APIs")
public class ShareController {
    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    @Operation(summary = "Create share link for a product")
    public ResponseEntity<ShareResponse> createShareLink(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ShareRequest request) {
        return ResponseEntity.ok(shareService.createShareLink(userId, request));
    }

    @GetMapping("/rewards")
    @Operation(summary = "Get user's share rewards (paginated)")
    public ResponseEntity<Page<ShareReward>> getUserRewards(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(shareService.getUserRewards(userId, pageable));
    }

    @GetMapping("/rewards/all")
    @Operation(summary = "Get user's share rewards (non-paginated)")
    public ResponseEntity<List<ShareReward>> getUserRewardsAll(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(shareService.getUserRewards(userId));
    }
}
