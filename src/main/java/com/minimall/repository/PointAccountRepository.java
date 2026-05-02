package com.minimall.repository;

import com.minimall.model.PointAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PointAccountRepository extends JpaRepository<PointAccount, String> {
    Optional<PointAccount> findByUserId(String userId);
}