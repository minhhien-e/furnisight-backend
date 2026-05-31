package com.furnisight.user.infrastructure.database.repository.impl.identity;

import com.furnisight.user.domain.enums.identity.VerificationType;
import com.furnisight.user.domain.repository.identity.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisOtpVerificationRepository implements OtpVerificationRepository {

    private static final String KEY_PREFIX = "otp:email:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String email, VerificationType type, String otpHash, Duration ttl) {
        redisTemplate.opsForValue().set(keyOf(email, type), otpHash, ttl);
    }

    @Override
    public Optional<String> findHashByEmailAndType(String email, VerificationType type) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(keyOf(email, type)));
    }

    @Override
    public void deleteByEmailAndType(String email, VerificationType type) {
        redisTemplate.delete(keyOf(email, type));
    }

    private String keyOf(String email, VerificationType type) {
        return KEY_PREFIX + type.name().toLowerCase() + ":" + email.toLowerCase();
    }
}
