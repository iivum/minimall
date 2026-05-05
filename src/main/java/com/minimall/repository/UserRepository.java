package com.minimall.repository;

import com.minimall.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByOpenid(String openid);
    Optional<User> findByPhone(String phone);
}