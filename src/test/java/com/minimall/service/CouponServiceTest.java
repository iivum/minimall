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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

    @Mock private CouponRepository couponRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;

    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository, userCouponRepository, userRepository, orderRepository);
    }

    @Test
    void createCoupon_savesAndReturnsCoupon() {
        CouponRequest request = new CouponRequest(
            "SAVE10", BigDecimal.valueOf(10), BigDecimal.valueOf(100),
            Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), 100, "CASH"
        );
        Coupon savedCoupon = new Coupon();
        savedCoupon.setId("coupon-1");
        savedCoupon.setCode("SAVE10");
        savedCoupon.setCouponType(Coupon.CouponType.CASH);
        savedCoupon.setDiscountAmount(BigDecimal.valueOf(10));
        savedCoupon.setMinOrderAmount(BigDecimal.valueOf(100));
        savedCoupon.setValidFrom(request.validFrom());
        savedCoupon.setValidUntil(request.validUntil());
        savedCoupon.setTotalQuantity(100);
        savedCoupon.setRemainingQuantity(100);
        savedCoupon.setActive(true);

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        CouponResponse response = couponService.createCoupon(request);

        assertNotNull(response);
        assertEquals("coupon-1", response.id());
        assertEquals("SAVE10", response.code());
        assertEquals("CASH", response.couponType());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
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
        assertEquals("coupon-1", result.get(0).id());
    }

    @Test
    void getAvailableCoupons_withPagination_returnsPagedCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("SAVE10");
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);

        Page<Coupon> page = new PageImpl<>(List.of(coupon));
        when(couponRepository.findByIsActiveTrue(any(PageRequest.class))).thenReturn(page);

        Page<CouponResponse> result = couponService.getAvailableCoupons(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
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
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(Collections.emptyList());
        when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(new UserCoupon());
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        CouponResponse response = couponService.claimCoupon("user-1", "coupon-1");

        assertNotNull(response);
        assertEquals("coupon-1", response.id());
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

    @Test
    void claimCoupon_throwsWhenUserNotFound() {
        when(userRepository.findById("invalid-user")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            couponService.claimCoupon("invalid-user", "coupon-1")
        );
    }

    @Test
    void claimCoupon_throwsWhenCouponNotFound() {
        User user = new User();
        user.setId("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("invalid-coupon")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            couponService.claimCoupon("user-1", "invalid-coupon")
        );
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
            couponService.claimCoupon("user-1", "coupon-1")
        );
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
            couponService.claimCoupon("user-1", "coupon-1")
        );
    }

    @Test
    void claimCoupon_throwsWhenNewUserCouponForExistingUser() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCouponType(Coupon.CouponType.NEW_USER);
        coupon.setRemainingQuantity(10);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(new com.minimall.model.Order()));

        assertThrows(IllegalStateException.class, () ->
            couponService.claimCoupon("user-1", "coupon-1")
        );
    }

    @Test
    void getUserCoupons_returnsUnusedCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("SAVE10");
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(BigDecimal.valueOf(10));
        coupon.setActive(true);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCoupon(coupon);
        userCoupon.setUsed(false);

        when(userCouponRepository.findByUserIdAndIsUsedFalse("user-1")).thenReturn(List.of(userCoupon));

        List<CouponResponse> result = couponService.getUserCoupons("user-1");

        assertEquals(1, result.size());
        assertEquals("coupon-1", result.get(0).id());
    }

    @Test
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