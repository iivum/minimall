package com.minimall.repository;

import com.minimall.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findByStatus(Order.Status status);
    List<Order> findByPayStatus(Order.PayStatus payStatus);
    Page<Order> findAll(Pageable pageable);
    long countByPayStatus(Order.PayStatus payStatus);
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.payStatus = :payStatus")
    BigDecimal sumTotalAmountByPayStatus(Order.PayStatus payStatus);
}