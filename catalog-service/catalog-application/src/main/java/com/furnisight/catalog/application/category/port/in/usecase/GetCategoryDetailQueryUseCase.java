package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.CategoryDetailResponseDto;
import com.furnisight.catalog.application.category.dto.GetCategoryDetailQuery;

public interface GetCategoryDetailQueryUseCase {
    CategoryDetailResponseDto execute(GetCategoryDetailQuery query);
}
