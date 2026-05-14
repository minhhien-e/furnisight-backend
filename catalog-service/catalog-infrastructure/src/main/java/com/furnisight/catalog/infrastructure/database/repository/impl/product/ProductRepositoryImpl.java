package com.furnisight.catalog.infrastructure.database.repository.impl.product;

import com.furnisight.catalog.infrastructure.database.repository.jpa.product.ProductJpaRepository;

import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.product.Product;
import lombok.RequiredArgsConstructor;
import com.furnisight.catalog.domain.enums.product.ProductStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public boolean existsByShopIdAndNameValue(UUID shopId, String name) {
        return jpaProductRepository.existsByShopIdAndNameValue(shopId, name);
    }

    @Override
    public boolean existsByShopIdAndNameValueAndIdNot(UUID shopId, String name, UUID id) {
        return jpaProductRepository.existsByShopIdAndNameValueAndIdNot(shopId, name, id);
    }

    @Override
    public List<Product> search(String query, UUID categoryId, Double minPrice, Double maxPrice, String status, int page, int size) {
        ProductStatus productStatus = status != null ? ProductStatus.valueOf(status) : null;
        return jpaProductRepository.searchProducts(query, categoryId, productStatus, PageRequest.of(page, size)).getContent();
    }

    @Override
    public List<Product> findTopByStatus(String status, int limit) {
        ProductStatus productStatus = status != null ? ProductStatus.valueOf(status) : null;
        return jpaProductRepository.findTop8ByProductStatusOrderByCreatedAtDesc(productStatus);
    }
}
