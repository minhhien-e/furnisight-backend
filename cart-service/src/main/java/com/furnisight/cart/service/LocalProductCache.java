package com.furnisight.cart.service;

import com.furnisight.catalog.ProductSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalProductCache {
    private static class CacheEntry {
        final ProductSummary value;
        final long expiryTime;
        
        CacheEntry(ProductSummary value, long ttlMs) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMs;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final Map<String, CacheEntry> productCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    public ProductSummary get(String key) {
        CacheEntry entry = productCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.value;
        }
        return null;
    }

    public void put(String key, ProductSummary product) {
        productCache.put(key, new CacheEntry(product, CACHE_TTL_MS));
    }
}
