package com.furnisight.catalog.domain.repository.category;

import com.furnisight.catalog.domain.entities.category.Category;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findById(UUID id);
    Category save(Category category);
    boolean existsByNameAndParentId(String name, UUID parentId);
    java.util.List<Category> findAll();
    java.util.List<Category> findAllByParentId(UUID parentId);
}
