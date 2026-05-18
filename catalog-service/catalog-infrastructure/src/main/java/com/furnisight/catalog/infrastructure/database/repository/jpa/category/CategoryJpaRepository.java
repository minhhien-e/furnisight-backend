package com.furnisight.catalog.infrastructure.database.repository.jpa.category;

import com.furnisight.catalog.domain.entities.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);

    boolean existsByNameValueAndParentId(String valueName, UUID parentId);

    List<Category> findByParentId(UUID parentId);
}
