package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import java.util.List;

public interface ListSubcategoriesUseCase {
    List<CategoryResponse> execute(String parentSlug);
}
