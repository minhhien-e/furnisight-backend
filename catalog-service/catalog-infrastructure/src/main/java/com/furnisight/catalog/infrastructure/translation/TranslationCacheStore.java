package com.furnisight.catalog.infrastructure.translation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class TranslationCacheStore {

    private final Cache<String, String> localCache;
    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    @Autowired
    public TranslationCacheStore(
            TranslationProperties properties,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider
    ) {
        this(properties, redisTemplateProvider.getIfAvailable());
    }

    TranslationCacheStore(
            TranslationProperties properties,
            StringRedisTemplate redisTemplate
    ) {
        this.localCache = Caffeine.newBuilder()
                .maximumSize(properties.getCacheMaximumSize())
                .expireAfterWrite(properties.getCacheTtl())
                .build();
        this.redisTemplate = redisTemplate;
        this.ttl = properties.getCacheTtl();
    }

    public Optional<String> get(String key) {
        String localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            return Optional.of(localValue);
        }

        if (redisTemplate == null) {
            return Optional.empty();
        }

        try {
            String redisValue = redisTemplate.opsForValue().get(key);
            if (redisValue != null) {
                localCache.put(key, redisValue);
                return Optional.of(redisValue);
            }
        } catch (Exception ignored) {
            // Fallback to local cache only when Redis is unavailable.
        }

        return Optional.empty();
    }

    public void put(String key, String value) {
        if (key == null || value == null) {
            return;
        }

        localCache.put(key, value);
        if (redisTemplate == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception ignored) {
            // Best effort: local cache remains the fallback.
        }
    }
}
