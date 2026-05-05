package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.MemberBenefitsResponse;
import com.minimall.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public ResponseEntity<MemberBenefitsResponse> getBenefits() {
        String userId = securityUtils.getCurrentUserId();
        MemberBenefitsResponse benefits = memberService.getBenefits(userId);
        return ResponseEntity.ok(benefits);
    }

    @PostMapping("/redeem")
    public ResponseEntity<Map<String, String>> redeem(@RequestBody Map<String, Object> request) {
        String userId = securityUtils.getCurrentUserId();
        String benefitType = (String) request.get("benefitType");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        memberService.redeem(userId, benefitType, amount);
        return ResponseEntity.ok(Map.of("message", "Redemption successful"));
    }
}