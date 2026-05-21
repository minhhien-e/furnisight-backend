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
}
