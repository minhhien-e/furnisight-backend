package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.dto.query.GetCategoryDetailQuery;

public interface GetCategoryDetailUseCase {
    CategoryResponse execute(GetCategoryDetailQuery query);
}
