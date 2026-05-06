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

    private CouponResponse createTestCouponResponse(String id, String code) {
        return new CouponResponse(
            id,
            code,
            "NEW_USER",
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(100),
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30),
            true,
            100
        );
    }

    @Test
    @WithMockUser
    void createCoupon_returnsCreatedCoupon() throws Exception {
        String couponId = "coupon-123";
        CouponResponse response = createTestCouponResponse(couponId, "NEWUSER10");
        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(response);

        String requestBody = """
            {
                "code": "NEWUSER10",
                "discountAmount": 10,
                "minOrderAmount": 100,
                "validFrom": "2024-01-01T00:00:00Z",
                "validUntil": "2024-12-31T23:59:59Z",
                "totalQuantity": 100,
                "couponType": "NEW_USER"
            }
            """;

        mockMvc.perform(post("/api/coupons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(couponId))
            .andExpect(jsonPath("$.code").value("NEWUSER10"))
            .andExpect(jsonPath("$.couponType").value("NEW_USER"));
    }

    @Test
    @WithMockUser
    void getAvailableCoupons_returnsCouponList() throws Exception {
        CouponResponse coupon1 = createTestCouponResponse("coupon-1", "DISCOUNT10");
        CouponResponse coupon2 = createTestCouponResponse("coupon-2", "DISCOUNT20");
        when(couponService.getAvailableCoupons()).thenReturn(List.of(coupon1, coupon2));

        mockMvc.perform(get("/api/coupons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("coupon-1"))
            .andExpect(jsonPath("$[0].code").value("DISCOUNT10"))
            .andExpect(jsonPath("$[1].id").value("coupon-2"))
            .andExpect(jsonPath("$[1].code").value("DISCOUNT20"));
    }

    @Test
    @WithMockUser
    void getNewUserCoupons_returnsNewUserCouponList() throws Exception {
        CouponResponse coupon = createTestCouponResponse("coupon-new", "NEWUSER");
        when(couponService.getNewUserCoupons()).thenReturn(List.of(coupon));

        mockMvc.perform(get("/api/coupons/new-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("coupon-new"))
            .andExpect(jsonPath("$[0].couponType").value("NEW_USER"));
    }

    @Test
    @WithMockUser
    void claimCoupon_returnsClaimedCoupon() throws Exception {
        String couponId = "coupon-123";
        String userId = "user-456";
        CouponResponse response = createTestCouponResponse(couponId, "CLAIMED");
        when(couponService.claimCoupon(userId, couponId)).thenReturn(response);

        mockMvc.perform(post("/api/coupons/{couponId}/claim", couponId)
                .with(csrf())
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(couponId));
    }

    @Test
    @WithMockUser
    void claimCoupon_returnsError_whenCouponExhausted() throws Exception {
        String couponId = "coupon-123";
        String userId = "user-456";
        when(couponService.claimCoupon(userId, couponId))
            .thenThrow(new IllegalStateException("Coupon exhausted"));

        mockMvc.perform(post("/api/coupons/{couponId}/claim", couponId)
                .with(csrf())
                .header("X-User-Id", userId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void claimCoupon_returnsError_whenAlreadyClaimed() throws Exception {
        String couponId = "coupon-123";
        String userId = "user-456";
        when(couponService.claimCoupon(userId, couponId))
            .thenThrow(new IllegalStateException("Already claimed"));

        mockMvc.perform(post("/api/coupons/{couponId}/claim", couponId)
                .with(csrf())
                .header("X-User-Id", userId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getMyCoupons_returnsUserCouponList() throws Exception {
        String userId = "user-123";
        CouponResponse coupon1 = createTestCouponResponse("coupon-1", "MYCOUPON1");
        CouponResponse coupon2 = createTestCouponResponse("coupon-2", "MYCOUPON2");
        when(couponService.getUserCoupons(userId)).thenReturn(List.of(coupon1, coupon2));

        mockMvc.perform(get("/api/coupons/my")
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("coupon-1"))
            .andExpect(jsonPath("$[1].id").value("coupon-2"));
    }

    @Test
    @WithMockUser
    void getMyCoupons_returnsEmptyList_whenNoCoupons() throws Exception {
        String userId = "user-123";
        when(couponService.getUserCoupons(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/coupons/my")
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAvailableCoupons_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/coupons"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void claimCoupon_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/coupons/coupon-123/claim")
                .with(csrf())
                .header("X-User-Id", "user-123"))
            .andExpect(status().isUnauthorized());
    }
}
