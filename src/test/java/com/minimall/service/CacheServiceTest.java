package com.minimall.service;

import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService Tests")
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Counter cacheHitCounter;

    @Mock
    private Counter cacheMissCounter;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheService = new CacheService(redisTemplate, cacheHitCounter, cacheMissCounter);
    }

    @Test
    @DisplayName("get returns empty when key not found")
    void get_keyNotFound_returnsEmpty() {
        when(valueOperations.get("product:123")).thenReturn(null);

        Optional<Object> result = cacheService.get("product:123", Object.class);

        assertThat(result).isEmpty();
        verify(cacheMissCounter).increment();
    }

    @Test
    @DisplayName("get returns cached value when found")
    void get_valueFound_returnsCached() {
        TestProduct product = new TestProduct();
        product.setName("Test Product");
        when(valueOperations.get("product:123")).thenReturn(product);

        Optional<TestProduct> result = cacheService.get("product:123", TestProduct.class);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Product");
        verify(cacheHitCounter).increment();
    }

    @Test
    @DisplayName("set stores value with default TTL")
    void set_value_storesWithDefaultTTL() {
        TestProduct product = new TestProduct();

        cacheService.set("product:123", product);

        verify(valueOperations).set(eq("product:123"), eq(product), any(Duration.class));
    }

    @Test
    @DisplayName("set stores null value with short TTL for cache penetration protection")
    void set_nullValue_storesWithShortTTL() {
        cacheService.set("product:null", null);

        verify(valueOperations).set(eq("product:null"), any(CacheService.NullValuePlaceholder.class), eq(Duration.ofSeconds(30)));
    }

    @Test
    @DisplayName("evict deletes key from cache")
    void evict_deletesKey() {
        cacheService.evict("product:123");

        verify(redisTemplate).delete("product:123");
    }

    @Test
    @DisplayName("exists returns true when key exists")
    void exists_keyExists_returnsTrue() {
        when(redisTemplate.hasKey("product:123")).thenReturn(true);

        boolean result = cacheService.exists("product:123");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("exists returns false when key does not exist")
    void exists_keyNotExists_returnsFalse() {
        when(redisTemplate.hasKey("product:123")).thenReturn(false);

        boolean result = cacheService.exists("product:123");

        assertThat(result).isFalse();
    }

    private static class TestProduct {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}