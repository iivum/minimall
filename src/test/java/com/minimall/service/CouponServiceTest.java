package com.minimall.service;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.model.Coupon;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserCoupon;
import com.minimall.repository.CouponRepository;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.UserCouponRepository;
import com.minimall.repository.UserRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository, userCouponRepository, userRepository, orderRepository);
    }

    @Test
    @DisplayName("getAvailableCoupons returns active coupons only")
    void getAvailableCoupons_returnsActiveCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("SAVE10");
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);

        when(couponRepository.findByIsActiveTrue()).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getAvailableCoupons();

        assertEquals(1, result.size());
        assertEquals("SAVE10", result.get(0).code());
        verify(couponRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAvailableCoupons with pagination returns paginated results")
    void getAvailableCoupons_paged_returnsPaginatedResults() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("SAVE10");
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Coupon> page = new PageImpl<>(List.of(coupon), pageable, 1);

        when(couponRepository.findByIsActiveTrue(pageable)).thenReturn(page);

        Page<CouponResponse> result = couponService.getAvailableCoupons(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("SAVE10", result.getContent().get(0).code());
    }

    @Test
    @DisplayName("claimCoupon successfully claims coupon for user")
    void claimCoupon_success() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("NEWUSER");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setRemainingQuantity(10);
        coupon.setActive(true);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of());
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        CouponResponse result = couponService.claimCoupon("user-1", "coupon-1");

        assertNotNull(result);
        assertEquals("NEWUSER", result.code());
        verify(userCouponRepository).save(any(UserCoupon.class));
        verify(couponRepository).save(coupon);
    }

    @Test
    @DisplayName("claimCoupon throws when coupon exhausted")
    void claimCoupon_exhausted_throws() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(0);
        coupon.setActive(true);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));

        assertThrows(IllegalStateException.class, () -> couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when already claimed")
    void claimCoupon_alreadyClaimed_throws() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(10);
        coupon.setActive(true);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when new user coupon claimed by existing user")
    void claimCoupon_newUserCoupon_byExistingUser_throws() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setRemainingQuantity(10);
        coupon.setActive(true);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(new Order()));

        assertThrows(IllegalStateException.class, () -> couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("getUserCoupons returns unused coupons for user")
    void getUserCoupons_returnsUnusedCoupons() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("SAVE10");
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);

        when(userCouponRepository.findByUserIdAndIsUsedFalse("user-1")).thenReturn(List.of(userCoupon));

        List<CouponResponse> result = couponService.getUserCoupons("user-1");

        assertEquals(1, result.size());
        assertEquals("SAVE10", result.get(0).code());
    }

    @Test
    @DisplayName("createCoupon saves and returns coupon response")
    void createCoupon_savesAndReturnsResponse() {
        Instant now = Instant.now();
        CouponRequest request = new CouponRequest(
            "SAVE20", BigDecimal.valueOf(20), BigDecimal.valueOf(100),
            now, now.plusSeconds(86400), 100, "CASH"
        );

        Coupon savedCoupon = new Coupon();
        savedCoupon.setId("new-coupon");
        savedCoupon.setCode("SAVE20");
        savedCoupon.setCouponType(Coupon.CouponType.CASH);
        savedCoupon.setDiscountAmount(BigDecimal.valueOf(20));
        savedCoupon.setRemainingQuantity(100);
        savedCoupon.setActive(true);

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        CouponResponse result = couponService.createCoupon(request);

        assertNotNull(result);
        assertEquals("SAVE20", result.code());
        assertEquals(100, result.remainingQuantity());
    }

    @Test
    @DisplayName("getNewUserCoupons returns new user coupons")
    void getNewUserCoupons_returnsNewUserCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("NEWUSER");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);

        when(couponRepository.findByCouponType(Coupon.CouponType.NEW_USER)).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getNewUserCoupons();

        assertEquals(1, result.size());
        assertEquals("NEW_USER", result.get(0).couponType());
    }
}