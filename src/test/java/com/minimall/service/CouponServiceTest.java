package com.minimall.service;

import com.minimall.dto.CouponRequest;
import com.minimall.dto.CouponResponse;
import com.minimall.model.Coupon;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    @DisplayName("createCoupon saves coupon and returns response")
    void createCoupon_savesAndReturns() {
        CouponRequest request = new CouponRequest(
            "CODE123",
            new BigDecimal("10.00"),
            new BigDecimal("100.00"),
            Instant.now(),
            Instant.now().plus(30, ChronoUnit.DAYS),
            100,
            "NEW_USER"
        );
        Coupon savedCoupon = new Coupon();
        savedCoupon.setId("coupon-1");
        savedCoupon.setCode("CODE123");
        savedCoupon.setDiscountAmount(new BigDecimal("10.00"));
        savedCoupon.setMinOrderAmount(new BigDecimal("100.00"));
        savedCoupon.setCouponType(Coupon.CouponType.NEW_USER);
        savedCoupon.setActive(true);
        savedCoupon.setRemainingQuantity(100);

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        CouponResponse response = couponService.createCoupon(request);

        assertNotNull(response);
        assertEquals("CODE123", response.code());
        assertEquals("10.00", response.discountAmount().toString());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("getAvailableCoupons returns active coupons")
    void getAvailableCoupons_returnsActiveCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("CODE123");
        coupon.setDiscountAmount(new BigDecimal("10.00"));
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setActive(true);
        coupon.setRemainingQuantity(50);

        when(couponRepository.findByIsActiveTrue()).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getAvailableCoupons();

        assertEquals(1, result.size());
        assertEquals("CODE123", result.get(0).code());
    }

    @Test
    @DisplayName("claimCoupon throws when user not found")
    void claimCoupon_userNotFound_throws() {
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when coupon not found")
    void claimCoupon_couponNotFound_throws() {
        User user = new User();
        user.setId("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when coupon exhausted")
    void claimCoupon_exhausted_throws() {
        User user = new User();
        user.setId("user-1");
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(0);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));

        assertThrows(IllegalStateException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when already claimed")
    void claimCoupon_alreadyClaimed_throws() {
        User user = new User();
        user.setId("user-1");
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(100);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when new user coupon claimed by existing user")
    void claimCoupon_existingUserNewUserCoupon_throws() {
        User user = new User();
        user.setId("user-1");
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(100);
        coupon.setCouponType(Coupon.CouponType.NEW_USER);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(new com.minimall.model.Order()));

        assertThrows(IllegalStateException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon decrements remaining quantity")
    void claimCoupon_decrementsRemainingQuantity() {
        User user = new User();
        user.setId("user-1");
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("CODE123");
        coupon.setRemainingQuantity(100);
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(new BigDecimal("10.00"));
        coupon.setMinOrderAmount(new BigDecimal("100.00"));
        coupon.setActive(true);
        coupon.setValidFrom(Instant.now().minus(1, ChronoUnit.DAYS));
        coupon.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS));

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(i -> i.getArgument(0));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        CouponResponse response = couponService.claimCoupon("user-1", "coupon-1");

        assertNotNull(response);
        assertEquals(99, coupon.getRemainingQuantity());
        verify(couponRepository).save(coupon);
    }

    @Test
    @DisplayName("getUserCoupons returns unused coupons for user")
    void getUserCoupons_returnsUnusedCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("CODE123");
        coupon.setDiscountAmount(new BigDecimal("10.00"));
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setActive(true);
        coupon.setRemainingQuantity(100);
        coupon.setValidFrom(Instant.now().minus(1, ChronoUnit.DAYS));
        coupon.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS));

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCoupon(coupon);

        when(userCouponRepository.findByUserIdAndIsUsedFalse("user-1")).thenReturn(List.of(userCoupon));

        List<CouponResponse> result = couponService.getUserCoupons("user-1");

        assertEquals(1, result.size());
        assertEquals("CODE123", result.get(0).code());
    }

    @Test
    @DisplayName("getNewUserCoupons returns new user type coupons")
    void getNewUserCoupons_returnsNewUserCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("NEWUSER123");
        coupon.setDiscountAmount(new BigDecimal("20.00"));
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setActive(true);
        coupon.setRemainingQuantity(50);

        when(couponRepository.findByCouponType(Coupon.CouponType.NEW_USER)).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getNewUserCoupons();

        assertEquals(1, result.size());
        assertEquals("NEWUSER123", result.get(0).code());
    }
}
