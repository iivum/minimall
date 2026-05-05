package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "Coupon", description = "Coupon management APIs")
public class CouponController {
    private final CouponService couponService;
    private final SecurityUtils securityUtils;

    public CouponController(CouponService couponService, SecurityUtils securityUtils) {
        this.couponService = couponService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new coupon (admin)")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @GetMapping
    @Operation(summary = "Get all available coupons")
    public ResponseEntity<List<CouponResponse>> getAvailableCoupons() {
        return ResponseEntity.ok(couponService.getAvailableCoupons());
    }

    @GetMapping("/new-user")
    @Operation(summary = "Get new user exclusive coupons")
    public ResponseEntity<List<CouponResponse>> getNewUserCoupons() {
        return ResponseEntity.ok(couponService.getNewUserCoupons());
    }

    @PostMapping("/{couponId}/claim")
    @Operation(summary = "Claim a coupon")
    public ResponseEntity<CouponResponse> claimCoupon(@PathVariable String couponId) {
        String userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new com.minimall.exception.UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(couponService.claimCoupon(userId, couponId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's claimed coupons")
    public ResponseEntity<List<CouponResponse>> getMyCoupons() {
        String userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new com.minimall.exception.UnauthorizedException("User not authenticated");
        }
        return ResponseEntity.ok(couponService.getUserCoupons(userId));
    }
}
