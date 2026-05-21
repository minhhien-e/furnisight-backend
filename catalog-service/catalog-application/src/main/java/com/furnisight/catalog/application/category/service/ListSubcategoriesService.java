package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.projection.CategoryDetailProjection;
import com.furnisight.catalog.application.category.port.in.usecase.ListSubcategoriesUseCase;
import com.furnisight.catalog.application.category.port.out.CategoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSubcategoriesService implements ListSubcategoriesUseCase {

    private final CategoryReadRepository categoryReadRepository;

    @Override
    public List<CategoryDetailProjection> execute(String parentSlug) {
        return categoryReadRepository.findSubcategoriesByParentSlug(parentSlug);
    }
}
