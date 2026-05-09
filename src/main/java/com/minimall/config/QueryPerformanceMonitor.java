package com.minimall.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QueryPerformanceMonitor {

    private static final long SLOW_QUERY_THRESHOLD_MS = 200;

    private final Timer repositoryQueryTimer;

    public QueryPerformanceMonitor(MeterRegistry registry) {
        this.repositoryQueryTimer = Timer.builder("minimall.repository.query.time")
                .description("Repository query execution time")
                .tag("type", "repository")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);
    }

    @Pointcut("execution(* com.minimall.repository..*.*(..))")
    public void repositoryMethods() {
    }

    @Around("repositoryMethods()")
    public Object monitorQueryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            repositoryQueryTimer.record(java.time.Duration.ofMillis(duration));

            if (duration > SLOW_QUERY_THRESHOLD_MS) {
                String methodName = joinPoint.getSignature().toShortString();
                System.err.printf("[WARN] Slow query detected: %s took %dms%n", methodName, duration);
            }
        }
    }
}