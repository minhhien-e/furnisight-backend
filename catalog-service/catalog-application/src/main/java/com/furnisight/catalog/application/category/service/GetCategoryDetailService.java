package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.CategoryDetailResponseDto;
import com.furnisight.catalog.application.category.dto.GetCategoryDetailQuery;
import com.furnisight.catalog.application.category.port.in.usecase.GetCategoryDetailQueryUseCase;
import com.furnisight.catalog.domain.repository.category.CategoryRepository;
import com.furnisight.catalog.domain.exceptions.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCategoryDetailService implements GetCategoryDetailQueryUseCase {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponseDto execute(GetCategoryDetailQuery query) {


        return categoryRepository.findById(query.getCategoryId()).map(
            c -> CategoryDetailResponseDto.builder()
                .id(c.getId())
                .name(c.getName().getValue())
                .slug(c.getSlug().getValue())
                .parantId(c.getParentId())
                .path(c.getPath())
                .build())
            .orElseThrow(() -> new CategoryNotFoundException(query.getCategoryId()));

    }
}
