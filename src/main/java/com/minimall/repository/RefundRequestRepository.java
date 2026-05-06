package com.minimall.repository;

import com.minimall.model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, String> {
    List<RefundRequest> findByOrderId(String orderId);
    List<RefundRequest> findByStatus(RefundRequest.Status status);
    Optional<RefundRequest> findByOrderIdAndStatus(String orderId, RefundRequest.Status status);
}