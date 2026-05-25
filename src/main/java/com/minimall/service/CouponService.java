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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CouponService(CouponRepository couponRepository,
                         UserCouponRepository userCouponRepository,
                         UserRepository userRepository,
                         OrderRepository orderRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public CouponResponse createCoupon(CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.code());
        coupon.setDiscountAmount(request.discountAmount());
        coupon.setMinOrderAmount(request.minOrderAmount());
        coupon.setValidFrom(request.validFrom());
        coupon.setValidUntil(request.validUntil());
        coupon.setTotalQuantity(request.totalQuantity());
        coupon.setRemainingQuantity(request.totalQuantity());
        coupon.setCouponType(Coupon.CouponType.valueOf(request.couponType()));
        Coupon saved = couponRepository.save(coupon);
        return toResponse(saved);
    }

    public Page<CouponResponse> getAvailableCoupons(Pageable pageable) {
        return couponRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    public Page<CouponResponse> getNewUserCoupons(Pageable pageable) {
        return couponRepository.findByCouponType(Coupon.CouponType.NEW_USER, pageable).map(this::toResponse);
    }

    @Transactional
    public CouponResponse claimCoupon(String userId, String couponId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        if (coupon.getRemainingQuantity() != null && coupon.getRemainingQuantity() <= 0) {
            throw new IllegalStateException("Coupon exhausted");
        }

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new IllegalStateException("Already claimed");
        }

        if (coupon.getCouponType() == Coupon.CouponType.NEW_USER) {
            boolean hasExistingOrders = !orderRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty();
            if (hasExistingOrders) {
                throw new IllegalStateException("Only new users can claim this coupon");
            }
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);
        userCouponRepository.save(userCoupon);

        if (coupon.getRemainingQuantity() != null) {
            coupon.setRemainingQuantity(coupon.getRemainingQuantity() - 1);
            couponRepository.save(coupon);
        }

        return toResponse(coupon);
    }

    public List<CouponResponse> getUserCoupons(String userId) {
        return userCouponRepository.findByUserIdAndIsUsedFalse(userId).stream()
            .map(uc -> toResponse(uc.getCoupon()))
            .toList();
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
            coupon.getId(),
            coupon.getCode(),
            coupon.getCouponType().name(),
            coupon.getDiscountAmount(),
            coupon.getMinOrderAmount(),
            coupon.getValidFrom(),
            coupon.getValidUntil(),
            coupon.isActive(),
            coupon.getRemainingQuantity()
        );
    }
}
