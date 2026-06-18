package com.furnisight.catalog.application.category.port.out;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import java.util.List;
import java.util.Optional;

public interface CategoryReadRepository {
    Optional<CategoryResponse> findCategoryDetailBySlug(String slug);

    List<CategoryResponse> findAllCategories();

    List<CategoryResponse> findRootCategories();

    List<CategoryResponse> findSubcategoriesByParentSlug(String parentSlug);

    Optional<CategoryResponse> findCategoryDetailById(java.util.UUID id);

    long countCategories();
}
