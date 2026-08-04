package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaProductRepository;

    @Override
    public Product save(Product product) {
        return jpaProductRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaProductRepository.findById(id);
    }

    @Override
    public boolean existsByNameValue(String name) {
        return jpaProductRepository.existsByNameValue(name);
    }

    @Override
    public Optional<UUID> findVariantIdBySku(String normalizedSku) {
        return jpaProductRepository.findVariantIdBySku(normalizedSku);
    }

    @Override
    public int updateVariantLowStockThreshold(UUID variantId, int lowStockThreshold) {
        return jpaProductRepository.updateVariantLowStockThreshold(variantId, lowStockThreshold);
    }

    @Override
    public int countByRoomTypeId(UUID roomTypeId) {
        return jpaProductRepository.countByRoomTypeId(roomTypeId);
    }
}
