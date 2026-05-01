package com.minimall.repository;

import com.minimall.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    Optional<UserSubscription> findByOpenid(String openid);
}