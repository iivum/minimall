package com.minimall.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter orderCreatedCounter(MeterRegistry registry) {
        return Counter.builder("minimall.orders.created")
            .description("Total number of orders created")
            .register(registry);
    }

    @Bean
    public Counter orderPaidCounter(MeterRegistry registry) {
        return Counter.builder("minimall.orders.paid")
            .description("Total number of paid orders")
            .register(registry);
    }

    @Bean
    public Counter couponRedeemedCounter(MeterRegistry registry) {
        return Counter.builder("minimall.coupons.redeemed")
            .description("Total number of coupons redeemed")
            .register(registry);
    }

    @Bean
    public Counter userRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("minimall.users.registered")
            .description("Total number of users registered")
            .register(registry);
    }

    @Bean
    public Timer orderProcessingTimer(MeterRegistry registry) {
        return Timer.builder("minimall.order.processing")
            .description("Time taken to process orders")
            .register(registry);
    }

    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("minimall.cache.hit")
            .description("Total cache hits")
            .register(registry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("minimall.cache.miss")
            .description("Total cache misses")
            .register(registry);
    }
}