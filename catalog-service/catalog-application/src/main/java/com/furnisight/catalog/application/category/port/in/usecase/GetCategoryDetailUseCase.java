package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import com.furnisight.catalog.application.category.dto.query.GetCategoryDetailQuery;

public interface GetCategoryDetailUseCase {
    CategoryDetailProjection execute(GetCategoryDetailQuery query);
}
