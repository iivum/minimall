package com.minimall.controller;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @DisplayName("createCoupon returns created coupon")
    void createCoupon_returnsCreatedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1",
            "SUMMER2024",
            "GENERAL",
            new BigDecimal("10.00"),
            new BigDecimal("100.00"),
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30),
            true,
            100
        );
        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/coupons")
                .with(csrf())
                .contentType("application/json")
                .content("""
                    {
                        "code": "SUMMER2024",
                        "discountAmount": 10.00,
                        "minOrderAmount": 100.00,
                        "validFrom": "2024-06-01T00:00:00Z",
                        "validUntil": "2024-07-01T00:00:00Z",
                        "totalQuantity": 100,
                        "couponType": "GENERAL"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("coupon-1"))
            .andExpect(jsonPath("$.code").value("SUMMER2024"));
    }

    @Test
    @WithMockUser
    @DisplayName("getAvailableCoupons returns coupon list")
    void getAvailableCoupons_returnsCouponList() throws Exception {
        List<CouponResponse> coupons = List.of(
            new CouponResponse("c1", "CODE1", "GENERAL", new BigDecimal("5"), new BigDecimal("50"),
                Instant.now(), Instant.now().plusSeconds(86400), true, 50),
            new CouponResponse("c2", "CODE2", "GENERAL", new BigDecimal("10"), new BigDecimal("100"),
                Instant.now(), Instant.now().plusSeconds(86400), true, 30)
        );
        when(couponService.getAvailableCoupons()).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("c1"))
            .andExpect(jsonPath("$[1].id").value("c2"));
    }

    @Test
    @WithMockUser
    @DisplayName("getNewUserCoupons returns new user coupons")
    void getNewUserCoupons_returnsNewUserCoupons() throws Exception {
        List<CouponResponse> coupons = List.of(
            new CouponResponse("c1", "NEWUSER", "NEW_USER", new BigDecimal("20"), new BigDecimal("0"),
                Instant.now(), Instant.now().plusSeconds(86400 * 7), true, 1000)
        );
        when(couponService.getNewUserCoupons()).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons/new-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].couponType").value("NEW_USER"));
    }

    @Test
    @WithMockUser
    @DisplayName("claimCoupon returns claimed coupon")
    void claimCoupon_returnsClaimedCoupon() throws Exception {
        CouponResponse response = new CouponResponse(
            "coupon-1", "SUMMER2024", "GENERAL",
            new BigDecimal("10.00"), new BigDecimal("100.00"),
            Instant.now(), Instant.now().plusSeconds(86400 * 30),
            true, 99
        );
        when(couponService.claimCoupon("user-1", "coupon-1")).thenReturn(response);

        mockMvc.perform(post("/api/coupons/coupon-1/claim")
                .with(csrf())
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("coupon-1"))
            .andExpect(jsonPath("$.remainingQuantity").value(99));
    }

    @Test
    @WithMockUser
    @DisplayName("getMyCoupons returns user's coupons")
    void getMyCoupons_returnsUserCoupons() throws Exception {
        List<CouponResponse> coupons = List.of(
            new CouponResponse("c1", "MYCODE", "GENERAL", new BigDecimal("5"), new BigDecimal("50"),
                Instant.now(), Instant.now().plusSeconds(86400), true, 10)
        );
        when(couponService.getUserCoupons("user-1")).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons/my")
                .header("X-User-Id", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].code").value("MYCODE"));
    }
}