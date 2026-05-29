package com.minimall.controller;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    @Mock
    private CouponService couponService;

    private CouponController couponController;

    @BeforeEach
    void setUp() {
        couponController = new CouponController(couponService);
    }

    private CouponResponse createCouponResponse(String id, String code) {
        return new CouponResponse(id, code, "DISCOUNT", new BigDecimal("10"),
            new BigDecimal("100"), Instant.now(), Instant.now().plusSeconds(86400),
            true, 100);
    }

    @Test
    @DisplayName("createCoupon returns created coupon")
    void createCoupon_success() {
        CouponRequest request = new CouponRequest("CODE123",
            new BigDecimal("10"), new BigDecimal("100"),
            Instant.now(), Instant.now().plusSeconds(86400),
            100, "NEW_USER");
        CouponResponse response = createCouponResponse("coupon-1", "CODE123");

        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(response);

        ResponseEntity<CouponResponse> result = couponController.createCoupon(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("coupon-1", result.getBody().id());
    }

    @Test
    @DisplayName("getAvailableCoupons returns paginated coupons")
    void getAvailableCoupons_success() {
        CouponResponse coupon = createCouponResponse("coupon-1", "Test");
        Pageable pageable = PageRequest.of(0, 10);
        Page<CouponResponse> page = new PageImpl<>(List.of(coupon), pageable, 1);

        when(couponService.getAvailableCoupons(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<CouponResponse>> result =
            couponController.getAvailableCoupons(0, 10);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("getAvailableCouponsAll returns all coupons")
    void getAvailableCouponsAll_success() {
        CouponResponse coupon = createCouponResponse("coupon-1", "Test");

        when(couponService.getAvailableCoupons()).thenReturn(List.of(coupon));

        ResponseEntity<List<CouponResponse>> result =
            couponController.getAvailableCouponsAll();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("getNewUserCoupons returns new user exclusive coupons")
    void getNewUserCoupons_success() {
        CouponResponse coupon = createCouponResponse("coupon-1", "New User");
        Pageable pageable = PageRequest.of(0, 10);
        Page<CouponResponse> page = new PageImpl<>(List.of(coupon), pageable, 1);

        when(couponService.getNewUserCoupons(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<CouponResponse>> result =
            couponController.getNewUserCoupons(0, 10);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("claimCoupon returns claimed coupon")
    void claimCoupon_success() {
        CouponResponse coupon = createCouponResponse("coupon-1", "Test");

        when(couponService.claimCoupon("user-1", "coupon-1")).thenReturn(coupon);

        ResponseEntity<CouponResponse> result =
            couponController.claimCoupon("user-1", "coupon-1");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("getMyCoupons returns user's coupons")
    void getMyCoupons_success() {
        CouponResponse coupon = createCouponResponse("coupon-1", "Mine");

        when(couponService.getUserCoupons("user-1")).thenReturn(List.of(coupon));

        ResponseEntity<List<CouponResponse>> result =
            couponController.getMyCoupons("user-1");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }
}