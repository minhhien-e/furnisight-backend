package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.port.in.usecase.ListSubcategoriesUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSubcategoriesService implements ListSubcategoriesUseCase {

    private final CategoryReadRepository categoryReadRepository;

    @Override
    @Cacheable(value = "categories_sub", key = "#p0")
    public List<CategoryResponse> execute(String parentSlug) {
        return categoryReadRepository.findSubcategoriesByParentSlug(parentSlug);
    }
}
