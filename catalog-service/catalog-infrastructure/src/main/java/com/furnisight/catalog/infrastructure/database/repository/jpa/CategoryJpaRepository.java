package com.furnisight.catalog.infrastructure.database.repository.jpa;

import com.furnisight.catalog.domain.entities.Category;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(CategorySlug slug);

    boolean existsByNameValueAndParentId(String valueName, UUID parentId);

    List<Category> findByParentId(UUID parentId);

    boolean existsSlug(CategorySlug slug);
}
