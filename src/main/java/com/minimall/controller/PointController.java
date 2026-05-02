package com.minimall.controller;

import com.minimall.config.SecurityUtils;
import com.minimall.dto.DeductPointsRequest;
import com.minimall.dto.PointAccountResponse;
import com.minimall.dto.PointTransactionResponse;
import com.minimall.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;
    private final SecurityUtils securityUtils;

    public PointController(PointService pointService, SecurityUtils securityUtils) {
        this.pointService = pointService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/account")
    public ResponseEntity<PointAccountResponse> getCurrentAccount() {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(PointAccountResponse.from(pointService.getAccount(userId)));
    }

    @GetMapping("/account/{userId}")
    public ResponseEntity<PointAccountResponse> getAccount(@PathVariable String userId) {
        return ResponseEntity.ok(PointAccountResponse.from(pointService.getAccountByUserId(userId)));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PointTransactionResponse>> getCurrentHistory() {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(pointService.getHistory(userId).stream()
            .map(PointTransactionResponse::from)
            .toList());
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PointTransactionResponse>> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(pointService.getHistory(userId).stream()
            .map(PointTransactionResponse::from)
            .toList());
    }

    @PostMapping("/sign-in")
    public ResponseEntity<PointAccountResponse> signIn() {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(PointAccountResponse.from(pointService.signIn(userId)));
    }

    @PostMapping("/earn/share/{shareId}")
    public ResponseEntity<PointAccountResponse> earnShareReward(@PathVariable String shareId) {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(PointAccountResponse.from(pointService.earnShareReward(userId, shareId)));
    }

    @PostMapping("/deduct")
    public ResponseEntity<PointAccountResponse> deduct(@RequestBody DeductPointsRequest request) {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(PointAccountResponse.from(
            pointService.deduct(userId, request.points(), request.orderNo(), request.description())));
    }

    @PostMapping("/redeem/coupon")
    public ResponseEntity<PointAccountResponse> redeemCoupon(@RequestBody DeductPointsRequest request) {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(PointAccountResponse.from(
            pointService.deduct(userId, request.points(), request.orderNo(),
                request.description() != null ? request.description() : "积分兑换优惠券")));
    }
}