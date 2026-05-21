package com.furnisight.media.api.service.impl;

import com.furnisight.media.api.service.IdempotencyService;
import com.furnisight.media.core.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {
    private final IdempotencyRepository idempotencyRepository;
    @Override
    public String get(String key) {
        return idempotencyRepository.get(key);
    }

    @Override
    public void save(String key, String value, long expireInSeconds) {
        idempotencyRepository.save(key, value, expireInSeconds);
    }
}
