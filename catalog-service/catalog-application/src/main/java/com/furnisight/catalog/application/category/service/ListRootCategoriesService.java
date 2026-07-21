package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.port.in.usecase.ListRootCategoriesUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.furnisight.catalog.application.product.service.ProductTranslationService;

@Service
@RequiredArgsConstructor
public class ListRootCategoriesService implements ListRootCategoriesUseCase {

    private final CategoryReadRepository categoryReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    @Cacheable(value = "categories_root", sync = true)
    public List<CategoryResponse> execute(String lang) {
        List<CategoryResponse> categories = categoryReadRepository.findRootCategories();
        return productTranslationService.localizeCategories(categories, lang);
    }
}
