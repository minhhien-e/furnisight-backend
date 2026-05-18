package com.furnisight.media.infrastructure.repository.impl;

import com.furnisight.media.core.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class IdempotencyRepositoryImpl implements IdempotencyRepository {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "idempotency:";

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + key);
    }

    @Override
    public void save(String key, String value, long expireInSeconds) {
        redisTemplate.opsForValue().set(KEY_PREFIX + key, value, expireInSeconds, TimeUnit.SECONDS);
    }
}
