package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {
    private final MemberService memberService;
    private final SecurityUtils securityUtils;

    public MembershipController(MemberService memberService, SecurityUtils securityUtils) {
        this.memberService = memberService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/benefits")
    public ResponseEntity<Map<String, Object>> getBenefits() {
        String userId = securityUtils.getCurrentUserId();
        Map<String, Object> benefits = memberService.getBenefits(userId);
        return ResponseEntity.ok(benefits);
    }

    @PostMapping("/redeem")
    public ResponseEntity<Map<String, Object>> redeem(@RequestBody Map<String, String> request) {
        String userId = securityUtils.getCurrentUserId();
        String rewardType = request.get("rewardType");
        Map<String, Object> result = memberService.redeem(userId, rewardType);
        return ResponseEntity.ok(result);
    }
}