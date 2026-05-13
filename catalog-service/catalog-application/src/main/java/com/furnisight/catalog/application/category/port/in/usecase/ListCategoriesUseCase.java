package com.furnisight.catalog.application.category.port.in.usecase;

import com.furnisight.catalog.application.category.dto.CategoryDetailResponseDto;
import java.util.List;

public interface ListCategoriesUseCase {
    List<CategoryDetailResponseDto> execute();
}
