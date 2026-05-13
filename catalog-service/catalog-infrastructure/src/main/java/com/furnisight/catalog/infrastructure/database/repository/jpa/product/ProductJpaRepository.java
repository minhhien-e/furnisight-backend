package com.furnisight.catalog.infrastructure.database.repository.jpa.product;

import com.furnisight.catalog.domain.entities.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import com.furnisight.catalog.domain.enums.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    boolean existsByShopIdAndNameValue(UUID shopId, String name);
    boolean existsByShopIdAndNameValueAndIdNot(UUID shopId, String name, UUID id);

    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR LOWER(p.name.value) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:status IS NULL OR p.productStatus = :status)")
    Page<Product> searchProducts(@Param("query") String query, 
                                 @Param("categoryId") UUID categoryId, 
                                 @Param("status") ProductStatus status, 
                                 Pageable pageable);

    List<Product> findTop8ByProductStatusOrderByCreatedAtDesc(ProductStatus status);
}
