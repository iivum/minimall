package com.minimall.controller;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    @Test
    @WithMockUser
    void createCoupon_returnsCreatedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE20", "CASH",
            BigDecimal.valueOf(20), BigDecimal.valueOf(100),
            Instant.now(), Instant.now().plusSeconds(86400 * 30),
            true, 100
        );

        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(response);

        String requestBody = """
            {
                "code": "SAVE20",
                "couponType": "CASH",
                "discountAmount": 20,
                "minOrderAmount": 100,
                "validFrom": "2026-01-01T00:00:00Z",
                "validUntil": "2026-12-31T23:59:59Z",
                "totalQuantity": 100
            }
            """;

        mockMvc.perform(post("/api/coupons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SAVE20"))
            .andExpect(jsonPath("$.discountAmount").value(20));
    }

    @Test
    @WithMockUser
    void getAvailableCoupons_returnsCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE20", "CASH",
            BigDecimal.valueOf(20), BigDecimal.valueOf(100),
            Instant.now(), Instant.now().plusSeconds(86400 * 30),
            true, 50
        );

        when(couponService.getAvailableCoupons()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/coupons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("SAVE20"));
    }

    @Test
    @WithMockUser
    void getNewUserCoupons_returnsNewUserExclusiveCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-new", "NEWUSER", "NEW_USER",
            BigDecimal.valueOf(50), BigDecimal.ZERO,
            Instant.now(), Instant.now().plusSeconds(86400 * 30),
            true, 500
        );

        when(couponService.getNewUserCoupons()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/coupons/new-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("NEWUSER"));
    }

    @Test
    @WithMockUser
    void claimCoupon_returnsClaimedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE20", "CASH",
            BigDecimal.valueOf(20), BigDecimal.valueOf(100),
            Instant.now(), Instant.now().plusSeconds(86400 * 30),
            true, 99
        );

        when(couponService.claimCoupon("user-1", "coupon-1")).thenReturn(response);

        mockMvc.perform(post("/api/coupons/coupon-1/claim")
                .with(csrf())
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remainingQuantity").value(99));
    }

    @Test
    @WithMockUser
    void getMyCoupons_returnsUserCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE20", "CASH",
            BigDecimal.valueOf(20), BigDecimal.valueOf(100),
            Instant.now(), Instant.now().plusSeconds(86400 * 30),
            true, 99
        );

        when(couponService.getUserCoupons("user-1")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/coupons/my")
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("SAVE20"));
    }
}