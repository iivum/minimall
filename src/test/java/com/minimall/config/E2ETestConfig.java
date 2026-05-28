package com.minimall.config;

import com.minimall.model.User;
import com.minimall.model.Product;
import com.minimall.repository.UserRepository;
import com.minimall.repository.ProductRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

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
    CommandLineRunner initTestData(UserRepository userRepository, ProductRepository productRepository) {
        return args -> {
            // Create test user
            User testUser = new User();
            testUser.setOpenid("testuser");
            testUser.setNickname("Test User");
            testUser.setPhone("13800138000");
            userRepository.save(testUser);

            // Create test products
            Product product1 = new Product();
            product1.setName("Test Product 1");
            product1.setDescription("Description for test product 1");
            product1.setPrice(BigDecimal.valueOf(99.99));
            product1.setStock(100);
            product1.setActive(true);
            product1.setImageUrl("https://example.com/product1.jpg");
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("Test Product 2");
            product2.setDescription("Description for test product 2");
            product2.setPrice(BigDecimal.valueOf(49.99));
            product2.setStock(50);
            product2.setActive(true);
            product2.setImageUrl("https://example.com/product2.jpg");
            productRepository.save(product2);
        };
    }
}
