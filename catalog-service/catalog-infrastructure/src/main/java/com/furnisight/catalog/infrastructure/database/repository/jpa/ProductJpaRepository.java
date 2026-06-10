package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    boolean existsByNameValue(String name);

    @Query("select variant.id from ProductVariant variant where upper(variant.sku) = :sku")
    Optional<UUID> findVariantIdBySku(@Param("sku") String normalizedSku);

    @Modifying
    @Query("update ProductVariant variant set variant.lowStockThreshold = :threshold where variant.id = :variantId")
    int updateVariantLowStockThreshold(@Param("variantId") UUID variantId, @Param("threshold") int threshold);
}
