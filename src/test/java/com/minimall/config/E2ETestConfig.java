package com.minimall.config;

import com.minimall.model.User;
import com.minimall.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class E2ETestConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public SimpleMeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    CommandLineRunner initTestData(UserRepository userRepository) {
        return args -> {
            User testUser = new User();
            testUser.setOpenid("testuser");
            testUser.setNickname("Test User");
            testUser.setPhone("13800138000");
            userRepository.save(testUser);
        };
    }
}