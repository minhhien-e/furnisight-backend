package com.furnisight.catalog.application.category.port.out;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryReadRepository {
    Optional<CategoryDetailProjection> findCategoryDetailBySlug(String slug);
    List<CategoryDetailProjection> findAllCategories();
}
