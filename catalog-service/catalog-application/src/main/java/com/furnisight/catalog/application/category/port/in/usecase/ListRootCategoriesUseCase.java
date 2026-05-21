package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import java.util.List;

public interface ListRootCategoriesUseCase {
    List<CategoryDetailProjection> execute();
}
