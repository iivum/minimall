package com.minimall.controller;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
@Import(TestSecurityConfig.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Test
    @WithMockUser
    void createCoupon_withValidRequest_returnsCreatedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-123",
            "SAVE20",
            "GENERAL",
            new BigDecimal("20.00"),
            new BigDecimal("100.00"),
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30),
            true,
            100
        );

        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(response);

        String requestBody = """
            {
                "code": "SAVE20",
                "discountAmount": 20.00,
                "minOrderAmount": 100.00,
                "validFrom": "2026-01-01T00:00:00Z",
                "validUntil": "2026-12-31T23:59:59Z",
                "totalQuantity": 100,
                "couponType": "GENERAL"
            }
            """;

        mockMvc.perform(post("/api/coupons")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("coupon-123"))
            .andExpect(jsonPath("$.code").value("SAVE20"))
            .andExpect(jsonPath("$.couponType").value("GENERAL"))
            .andExpect(jsonPath("$.remainingQuantity").value(100));
    }

    @Test
    @WithMockUser
    void getAvailableCoupons_returnsCouponList() throws Exception {
        List<CouponResponse> coupons = List.of(
            new CouponResponse("1", "CODE1", "GENERAL", new BigDecimal("10"), new BigDecimal("50"), Instant.now(), Instant.now().plusSeconds(86400), true, 50),
            new CouponResponse("2", "CODE2", "NEW_USER", new BigDecimal("15"), new BigDecimal("80"), Instant.now(), Instant.now().plusSeconds(86400), true, 30)
        );

        when(couponService.getAvailableCoupons()).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].code").value("CODE1"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[1].couponType").value("NEW_USER"));
    }

    @Test
    @WithMockUser
    void getNewUserCoupons_returnsNewUserExclusiveCoupons() throws Exception {
        List<CouponResponse> coupons = List.of(
            new CouponResponse("new-1", "NEWUSER10", "NEW_USER", new BigDecimal("10"), new BigDecimal("50"), Instant.now(), Instant.now().plusSeconds(86400), true, 20)
        );

        when(couponService.getNewUserCoupons()).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons/new-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].couponType").value("NEW_USER"));
    }

    @Test
    @WithMockUser
    void claimCoupon_withValidRequest_returnsClaimedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-123",
            "CLAIMED",
            "GENERAL",
            new BigDecimal("20.00"),
            new BigDecimal("100.00"),
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30),
            true,
            99
        );

        when(couponService.claimCoupon("user-123", "coupon-123")).thenReturn(response);

        mockMvc.perform(post("/api/coupons/coupon-123/claim")
                .with(csrf())
                .header("X-User-Id", "user-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("coupon-123"))
            .andExpect(jsonPath("$.remainingQuantity").value(99));
    }

    @Test
    @WithMockUser
    void getMyCoupons_returnsUserClaimedCoupons() throws Exception {
        List<CouponResponse> coupons = List.of(
            new CouponResponse("owned-1", "MYCODE", "GENERAL", new BigDecimal("5"), new BigDecimal("30"), Instant.now(), Instant.now().plusSeconds(86400), true, 10)
        );

        when(couponService.getUserCoupons("user-456")).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons/my")
                .header("X-User-Id", "user-456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("owned-1"));
    }
}