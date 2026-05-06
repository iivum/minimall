package com.minimall.repository;

import com.minimall.model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<RefundRequest, String> {
    List<RefundRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    List<RefundRequest> findByOrderId(String orderId);
    Optional<RefundRequest> findByOrderNo(String orderNo);
    List<RefundRequest> findByStatus(RefundRequest.Status status);
    List<RefundRequest> findAllByOrderByCreatedAtDesc();
}
