package com.furniro.MessageService.service.Other;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    
    private final StringRedisTemplate caching;
    private static final long OTP_VALID_DURATION = 5;

    @Async
    public void addData(
        String key,
        String value,
        long timeout,
        TimeUnit unit) {
        caching.opsForValue().set(key, value, timeout, unit);
    }

    @Async
    public void removeData(String key) {
        caching.delete(key);
    }

    public boolean isCaching(String key) {
        return caching.hasKey(key);
    }

    public String getData(String key) {
        return caching.opsForValue().get(key);
    }

    public String generateOtp(String key) {
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        caching.opsForValue().set(key, otp, OTP_VALID_DURATION, TimeUnit.MINUTES);
        return otp;
    }

    public boolean verifyOtp(String key, String otp) {
        String storedOtp = caching.opsForValue().get(key);
        if(storedOtp != null && storedOtp.equals(otp)) {
            caching.delete(key);
            return true;
        }
        return false;
    }

}
