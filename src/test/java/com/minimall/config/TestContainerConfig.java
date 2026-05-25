package com.minimall.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MavenImageName;

@TestConfiguration
public class TestContainerConfig {

    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(MavenImageName.parse("postgres:16").asCompatibleSubstituteFor("postgres"));
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry, PostgreSQLContainer<?> postgresContainer) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}