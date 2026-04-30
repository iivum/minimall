package com.minimall.service;

import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final Duration NULL_VALUE_TTL = Duration.ofSeconds(30);

    public CacheService(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("cacheHitCounter") Counter cacheHitCounter,
            @Qualifier("cacheMissCounter") Counter cacheMissCounter) {
        this.redisTemplate = redisTemplate;
        this.cacheHitCounter = cacheHitCounter;
        this.cacheMissCounter = cacheMissCounter;
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            cacheMissCounter.increment();
            return Optional.empty();
        }
        if (value instanceof NullValuePlaceholder) {
            return Optional.empty();
        }
        cacheHitCounter.increment();
        return Optional.of(type.cast(value));
    }

    public void set(String key, Object value) {
        if (value == null) {
            redisTemplate.opsForValue().set(key, NullValuePlaceholder.INSTANCE, NULL_VALUE_TTL);
        } else {
            redisTemplate.opsForValue().set(key, value, DEFAULT_TTL);
        }
    }

    public void set(String key, Object value, Duration ttl) {
        if (value == null) {
            redisTemplate.opsForValue().set(key, NullValuePlaceholder.INSTANCE, NULL_VALUE_TTL);
        } else {
            redisTemplate.opsForValue().set(key, value, ttl);
        }
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private static class NullValuePlaceholder {
        static final NullValuePlaceholder INSTANCE = new NullValuePlaceholder();
        private NullValuePlaceholder() {}
    }
}