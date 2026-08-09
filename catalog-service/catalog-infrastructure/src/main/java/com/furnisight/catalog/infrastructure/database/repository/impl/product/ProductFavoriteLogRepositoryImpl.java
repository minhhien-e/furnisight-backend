package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.furnisight.catalog.domain.entities.ProductFavoriteLog;
import com.furnisight.catalog.domain.repository.ProductFavoriteLogRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.ProductFavoriteLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFavoriteLogRepositoryImpl implements ProductFavoriteLogRepository {

    private final ProductFavoriteLogJpaRepository jpaRepository;

    @Override
    public void save(ProductFavoriteLog log) {
        jpaRepository.save(log);
    }

    @Override
    public void deleteByUserIdAndProductId(java.util.UUID userId, java.util.UUID productId) {
        jpaRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
