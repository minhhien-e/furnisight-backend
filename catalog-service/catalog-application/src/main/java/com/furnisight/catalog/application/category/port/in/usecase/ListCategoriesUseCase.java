package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import java.util.List;

public interface ListCategoriesUseCase {
    List<CategoryResponse> execute();
}
