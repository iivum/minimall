package com.minimall.repository;

import com.minimall.model.ShareReward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShareRewardRepository extends JpaRepository<ShareReward, String> {
    List<ShareReward> findBySharerId(String sharerId);
    Page<ShareReward> findBySharerId(String sharerId, Pageable pageable);
    List<ShareReward> findByOrderId(String orderId);
}
