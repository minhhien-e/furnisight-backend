package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.port.in.usecase.ListRootCategoriesUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListRootCategoriesService implements ListRootCategoriesUseCase {

    private final CategoryReadRepository categoryReadRepository;

    @Override
    public List<CategoryResponse> execute() {
        return categoryReadRepository.findRootCategories();
    }
}
