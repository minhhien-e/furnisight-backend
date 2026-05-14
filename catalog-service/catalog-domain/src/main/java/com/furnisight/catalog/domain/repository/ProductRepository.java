package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.entities.product.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    boolean existsByShopIdAndNameValue(UUID shopId, String name);
    boolean existsByShopIdAndNameValueAndIdNot(UUID shopId, String name, UUID id);
    java.util.List<Product> search(String query, UUID categoryId, Double minPrice, Double maxPrice, String status, int page, int size);
    java.util.List<Product> findTopByStatus(String status, int limit);
}
