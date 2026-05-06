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
    @DisplayName("createCoupon creates and returns coupon")
    void createCoupon_returnsCoupon() {
        CouponRequest request = new CouponRequest(
            "SUMMER2024",
            new BigDecimal("10.00"),
            new BigDecimal("100.00"),
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30),
            100,
            "CASH"
        );

        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon saved = invocation.getArgument(0);
            saved.setId("coupon-1");
            return saved;
        });

        CouponResponse result = couponService.createCoupon(request);

        assertNotNull(result);
        assertEquals("coupon-1", result.id());
        assertEquals("SUMMER2024", result.code());
    }

    @Test
    @DisplayName("getAvailableCoupons returns active coupons")
    void getAvailableCoupons_returnsActiveCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("ACTIVE");
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setActive(true);

        when(couponRepository.findByIsActiveTrue()).thenReturn(List.of(coupon));

        List<CouponResponse> result = couponService.getAvailableCoupons();

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).code());
    }

    @Test
    @DisplayName("claimCoupon throws when coupon exhausted")
    void claimCoupon_exhausted_throwsException() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(0);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));

        assertThrows(IllegalStateException.class, () -> couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon throws when already claimed")
    void claimCoupon_alreadyClaimed_throwsException() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(10);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon for new user coupon checks order history")
    void claimCoupon_newUserCoupon_checksOrderHistory() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setRemainingQuantity(10);
        coupon.setCouponType(Coupon.CouponType.NEW_USER);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(new com.minimall.model.Order()));

        assertThrows(IllegalStateException.class, () -> couponService.claimCoupon("user-1", "coupon-1"));
    }

    @Test
    @DisplayName("claimCoupon success decrements remaining quantity")
    void claimCoupon_success_decrementsQuantity() {
        User user = new User();
        user.setId("user-1");

        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("NEWUSER");
        coupon.setRemainingQuantity(10);
        coupon.setCouponType(Coupon.CouponType.CASH);
        coupon.setDiscountAmount(new BigDecimal("5"));

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(couponRepository.findById("coupon-1")).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUserIdAndCouponId("user-1", "coupon-1")).thenReturn(false);
        when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(i -> i.getArgument(0));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        CouponResponse result = couponService.claimCoupon("user-1", "coupon-1");

        assertNotNull(result);
        assertEquals(9, coupon.getRemainingQuantity());
    }

    @Test
    @DisplayName("getUserCoupons returns user's unused coupons")
    void getUserCoupons_returnsUnusedCoupons() {
        Coupon coupon = new Coupon();
        coupon.setId("coupon-1");
        coupon.setCode("MYCOUPON");
        coupon.setCouponType(Coupon.CouponType.CASH);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCoupon(coupon);
        userCoupon.setUsed(false);

        when(userCouponRepository.findByUserIdAndIsUsedFalse("user-1")).thenReturn(List.of(userCoupon));

        List<CouponResponse> result = couponService.getUserCoupons("user-1");

        assertEquals(1, result.size());
        assertEquals("MYCOUPON", result.get(0).code());
    }
}