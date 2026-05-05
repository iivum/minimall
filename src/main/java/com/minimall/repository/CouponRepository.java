package com.minimall.repository;

import com.minimall.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, String> {
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByCouponType(Coupon.CouponType couponType);
    List<Coupon> findByIsActiveTrue();
}
