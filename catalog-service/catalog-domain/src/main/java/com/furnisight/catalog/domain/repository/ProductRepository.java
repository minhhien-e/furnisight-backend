package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.entities.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    boolean existsByNameValue(String name);
    Optional<UUID> findVariantIdBySku(String normalizedSku);
    int updateVariantLowStockThreshold(UUID variantId, int lowStockThreshold);
}
