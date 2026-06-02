package com.furnisight.catalog.application.category.port.out;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import java.util.List;
import java.util.Optional;

public interface CategoryReadRepository {
    Optional<CategoryDetailProjection> findCategoryDetailBySlug(String slug);

    List<CategoryDetailProjection> findAllCategories();

    List<CategoryDetailProjection> findRootCategories();

    List<CategoryDetailProjection> findSubcategoriesByParentSlug(String parentSlug);

    Optional<CategoryDetailProjection> findCategoryDetailById(java.util.UUID id);

    long countCategories();
}
