package com.minimall.repository;

import com.minimall.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, String> {
    List<UserCoupon> findByUserId(String userId);
    List<UserCoupon> findByUserIdAndIsUsedFalse(String userId);
    Optional<UserCoupon> findByUserIdAndCouponId(String userId, String couponId);
    boolean existsByUserIdAndCouponId(String userId, String couponId);
}
