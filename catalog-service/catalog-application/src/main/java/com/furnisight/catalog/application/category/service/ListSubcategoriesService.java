package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.port.in.usecase.ListSubcategoriesUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.furnisight.catalog.application.product.service.ProductTranslationService;

@Service
@RequiredArgsConstructor
public class ListSubcategoriesService implements ListSubcategoriesUseCase {

    private final CategoryReadRepository categoryReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    @Cacheable(value = "categories_sub", key = "#p0 + '_' + #p1", sync = true)
    public List<CategoryResponse> execute(String parentSlug, String lang) {
        List<CategoryResponse> categories = categoryReadRepository.findSubcategoriesByParentSlug(parentSlug);
        return productTranslationService.localizeCategories(categories, lang);
    }
}
