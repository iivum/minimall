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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
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
    void createCoupon_savesAndReturnsCoupon() {
        CouponRequest request = new CouponRequest(
            "CODE123",
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(100),
            Instant.now(),
            Instant.now().plusSeconds(86400),
            100,
            "NEW_USER"
        );

        Coupon savedCoupon = new Coupon();
        savedCoupon.setId("coupon-1");
        savedCoupon.setCode("CODE123");
        savedCoupon.setDiscountAmount(BigDecimal.valueOf(10));
        savedCoupon.setCouponType(Coupon.CouponType.NEW_USER);
        savedCoupon.setActive(true);

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        CouponResponse result = couponService.createCoupon(request);

        assertNotNull(result);
        assertEquals("CODE123", result.code());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void getAvailableCoupons_returnsActiveCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("CODE123");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setActive(true);

        when(couponRepository.findByIsActiveTrue()).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getAvailableCoupons();

        assertEquals(1, result.size());
        assertEquals("CODE123", result.get(0).code());
    }

    @Test
    void getNewUserCoupons_returnsNewUserCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("NEWUSER123");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setActive(true);

        when(couponRepository.findByCouponType(Coupon.CouponType.NEW_USER)).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getNewUserCoupons();

        assertEquals(1, result.size());
        assertEquals("NEW_USER", result.get(0).couponType());
    }

    @Test
    void claimCoupon_claimsSuccessfully() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("CODE123");
        coupon.setRemainingQuantity(10);
        coupon.setCouponType(Coupon.CouponType.NEW_USER);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of());
        when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(i -> i.getArgument(0));

        CouponResponse result = couponService.claimCoupon("user-1", "coupon-1");

        assertNotNull(result);
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

    @Test
    void claimCoupon_throwsWhenCouponExhausted() {
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
    void claimCoupon_throwsWhenAlreadyClaimed() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(10);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    void claimCoupon_throwsWhenNotNewUser() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(new com.minimall.model.Order()));

        assertThrows(IllegalStateException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    void getUserCoupons_returnsUnusedCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("CODE123");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setActive(true);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCoupon(coupon);

        when(userCouponRepository.findByUserIdAndIsUsedFalse("user-1")).thenReturn(List.of(userCoupon));

        List<CouponResponse> result = couponService.getUserCoupons("user-1");

        assertEquals(1, result.size());
    }
}