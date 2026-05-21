package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    boolean existsByNameValue(String name);
}
