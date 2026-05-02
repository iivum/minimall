package com.minimall.controller;

import com.minimall.model.PointAccount;
import com.minimall.model.PointTransaction;
import com.minimall.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/account/{userId}")
    public ResponseEntity<PointAccount> getAccount(@PathVariable String userId) {
        return ResponseEntity.ok(pointService.getAccount(userId));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PointTransaction>> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(pointService.getHistory(userId));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<PointAccount> signIn(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        return ResponseEntity.ok(pointService.signIn(userId));
    }

    @PostMapping("/earn/order")
    public ResponseEntity<PointAccount> earnFromOrder(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String orderId = (String) request.get("orderId");
        return ResponseEntity.ok(pointService.earnFromOrder(userId, amount, orderId));
    }

    @PostMapping("/earn/share/{shareId}")
    public ResponseEntity<PointAccount> earnFromShare(@PathVariable String shareId,
                                                      @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        return ResponseEntity.ok(pointService.earnFromShare(userId, shareId));
    }

    @PostMapping("/deduct")
    public ResponseEntity<PointAccount> deduct(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String description = (String) request.get("description");
        return ResponseEntity.ok(pointService.deduct(userId, amount, description));
    }

    @PostMapping("/redeem/coupon")
    public ResponseEntity<PointAccount> redeemCoupon(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String couponId = request.get("couponId");
        return ResponseEntity.ok(pointService.redeemCoupon(userId, couponId));
    }
}