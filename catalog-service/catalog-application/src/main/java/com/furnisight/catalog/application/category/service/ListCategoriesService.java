package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.CategoryDetailResponseDto;
import com.furnisight.catalog.application.category.port.in.usecase.ListCategoriesUseCase;
import com.furnisight.catalog.domain.entities.category.Category;
import com.furnisight.catalog.domain.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListCategoriesService implements ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDetailResponseDto> execute() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private CategoryDetailResponseDto mapToDto(Category category) {
        return CategoryDetailResponseDto.builder()
                .id(category.getId())
                .name(category.getName().getValue())
                .slug(category.getSlug().getValue())
                .path(category.getPath())
                .parentId(category.getParentId())
                .build();
    }
}
