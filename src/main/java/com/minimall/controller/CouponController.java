package com.minimall.controller;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "Coupon", description = "Coupon management APIs")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @Operation(summary = "Create a new coupon (admin)")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @GetMapping
    @Operation(summary = "Get all available coupons")
    public ResponseEntity<Page<CouponResponse>> getAvailableCoupons(Pageable pageable) {
        return ResponseEntity.ok(couponService.getAvailableCoupons(pageable));
    }

    @GetMapping("/new-user")
    @Operation(summary = "Get new user exclusive coupons")
    public ResponseEntity<Page<CouponResponse>> getNewUserCoupons(Pageable pageable) {
        return ResponseEntity.ok(couponService.getNewUserCoupons(pageable));
    }

    @PostMapping("/{couponId}/claim")
    @Operation(summary = "Claim a coupon")
    public ResponseEntity<CouponResponse> claimCoupon(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String couponId) {
        return ResponseEntity.ok(couponService.claimCoupon(userId, couponId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's claimed coupons")
    public ResponseEntity<List<CouponResponse>> getMyCoupons(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(couponService.getUserCoupons(userId));
    }
}
