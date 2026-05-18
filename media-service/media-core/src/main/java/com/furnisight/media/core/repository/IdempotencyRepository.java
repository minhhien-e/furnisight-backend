package com.furnisight.media.core.repository;

public interface IdempotencyRepository {
    String get(String key);

    void save(String key,String value, long expireInSeconds);
}
