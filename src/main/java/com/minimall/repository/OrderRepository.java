package com.minimall.repository;

import com.minimall.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items WHERE o.orderNo = :orderNo")
    Optional<Order> findByOrderNo(String orderNo);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findById(String id);
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    List<Order> findByStatus(Order.Status status);
    List<Order> findByPayStatus(Order.PayStatus payStatus);
    Page<Order> findAll(Pageable pageable);
    long countByPayStatus(Order.PayStatus payStatus);
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.payStatus = :payStatus")
    BigDecimal sumTotalAmountByPayStatus(Order.PayStatus payStatus);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:payStatus IS NULL OR o.payStatus = :payStatus) AND " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Order> findByFilters(
            @Param("status") Order.Status status,
            @Param("payStatus") Order.PayStatus payStatus,
            @Param("userId") String userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}