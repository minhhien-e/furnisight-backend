package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.entities.Category;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Optional<Category> findBySlug(CategorySlug slug);
    Optional<Category> findById(UUID id);
    Category save(Category category);
    boolean existsByNameAndParentId(String name, UUID parentId);
    boolean existsSlug(CategorySlug slug);
}
