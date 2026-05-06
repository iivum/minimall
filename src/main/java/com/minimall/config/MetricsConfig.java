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
                .tag("type", "order")
                .register(registry);
    }

    @Bean
    public Counter orderPaidCounter(MeterRegistry registry) {
        return Counter.builder("minimall.orders.paid")
                .description("Total number of orders paid successfully")
                .tag("type", "payment")
                .register(registry);
    }

    @Bean
    public Counter orderFailedCounter(MeterRegistry registry) {
        return Counter.builder("minimall.orders.failed")
                .description("Total number of failed orders")
                .tag("type", "payment")
                .register(registry);
    }

    @Bean
    public Counter paymentSuccessCounter(MeterRegistry registry) {
        return Counter.builder("minimall.payment.success")
                .description("Total number of successful payments")
                .tag("type", "payment")
                .register(registry);
    }

    @Bean
    public Counter paymentFailureCounter(MeterRegistry registry) {
        return Counter.builder("minimall.payment.failure")
                .description("Total number of failed payments")
                .tag("type", "payment")
                .register(registry);
    }

    @Bean
    public Counter payment_success_counter(MeterRegistry registry) {
        return Counter.builder("minimall_payment_success")
                .description("Total number of successful payments (underscore naming)")
                .tag("type", "payment")
                .register(registry);
    }

    @Bean
    public Counter payment_failure_counter(MeterRegistry registry) {
        return Counter.builder("minimall_payment_failure")
                .description("Total number of failed payments (underscore naming)")
                .tag("type", "payment")
                .register(registry);
    }

    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("minimall.api.response.time")
                .description("API response time")
                .tag("type", "http")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer paymentProcessingTimer(MeterRegistry registry) {
        return Timer.builder("minimall.payment.processing.time")
                .description("Payment processing time")
                .tag("type", "payment")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }
}
