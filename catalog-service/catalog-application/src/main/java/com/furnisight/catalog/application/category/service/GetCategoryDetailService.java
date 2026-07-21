package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.response.CategoryResponse;
import com.furnisight.catalog.application.category.dto.query.GetCategoryDetailQuery;
import com.furnisight.catalog.application.category.port.in.usecase.GetCategoryDetailUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import com.furnisight.catalog.domain.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furnisight.catalog.application.product.service.ProductTranslationService;

@Service
@RequiredArgsConstructor
public class GetCategoryDetailService implements GetCategoryDetailUseCase {
    private final CategoryReadRepository categoryReadRepository;
    private final ProductTranslationService productTranslationService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "category_detail", key = "#p0.slug + '_' + #p0.lang", sync = true)
    public CategoryResponse execute(GetCategoryDetailQuery query) {
        CategoryResponse category = categoryReadRepository.findCategoryDetailBySlug(query.getSlug())
            .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        return productTranslationService.localizeCategory(category, query.getLang());
    }
}
