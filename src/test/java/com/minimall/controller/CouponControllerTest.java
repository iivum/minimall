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
import java.time.temporal.ChronoUnit;
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
            "coupon-1", "SAVE10", "CASH", BigDecimal.valueOf(10),
            BigDecimal.valueOf(100), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
            true, 100
        );

        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(response);

        String requestBody = """
            {
                "code": "SAVE10",
                "discountAmount": 10.00,
                "minOrderAmount": 100.00,
                "validFrom": "2026-01-01T00:00:00Z",
                "validUntil": "2026-12-31T23:59:59Z",
                "totalQuantity": 100,
                "couponType": "CASH"
            }
            """;

        mockMvc.perform(post("/api/coupons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("coupon-1"))
            .andExpect(jsonPath("$.code").value("SAVE10"));
    }

    @Test
    @WithMockUser
    void getAvailableCoupons_returnsPageOfCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE10", "CASH", BigDecimal.valueOf(10),
            BigDecimal.valueOf(100), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
            true, 100
        );

        when(couponService.getAvailableCoupons(any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/coupons")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value("coupon-1"));
    }

    @Test
    @WithMockUser
    void getAvailableCouponsAll_returnsAllCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE10", "CASH", BigDecimal.valueOf(10),
            BigDecimal.valueOf(100), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
            true, 100
        );

        when(couponService.getAvailableCoupons()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/coupons/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("coupon-1"));
    }

    @Test
    @WithMockUser
    void getNewUserCoupons_returnsNewUserCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "NEWUSER", "NEW_USER", BigDecimal.valueOf(20),
            BigDecimal.valueOf(200), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
            true, 50
        );

        when(couponService.getNewUserCoupons(any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/coupons/new-user")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].couponType").value("NEW_USER"));
    }

    @Test
    @WithMockUser
    void claimCoupon_returnsClaimedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "NEWUSER", "NEW_USER", BigDecimal.valueOf(20),
            BigDecimal.valueOf(200), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
            true, 49
        );

        when(couponService.claimCoupon("user-1", "coupon-1")).thenReturn(response);

        mockMvc.perform(post("/api/coupons/coupon-1/claim")
                .with(csrf())
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("coupon-1"))
            .andExpect(jsonPath("$.remainingQuantity").value(49));
    }

    @Test
    @WithMockUser
    void getMyCoupons_returnsUserCoupons() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SAVE10", "CASH", BigDecimal.valueOf(10),
            BigDecimal.valueOf(100), Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
            true, 100
        );

        when(couponService.getUserCoupons("user-1")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/coupons/my")
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("coupon-1"));
    }
}