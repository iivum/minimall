package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.ShareRequest;
import com.minimall.dto.ShareResponse;
import com.minimall.model.ShareReward;
import com.minimall.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/share")
@Tag(name = "Share", description = "Share reward APIs")
public class ShareController {
    private final ShareService shareService;
    private final SecurityUtils securityUtils;

    public ShareController(ShareService shareService, SecurityUtils securityUtils) {
        this.shareService = shareService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @Operation(summary = "Create share link for a product")
    public ResponseEntity<ShareResponse> createShareLink(@RequestBody ShareRequest request) {
        String userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new com.minimall.exception.UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(shareService.createShareLink(userId, request));
    }

    @GetMapping("/rewards")
    @Operation(summary = "Get user's share rewards")
    public ResponseEntity<List<ShareReward>> getUserRewards() {
        String userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new com.minimall.exception.UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(shareService.getUserRewards(userId));
    }
}
