package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.command.CreateCategoryCommand;
import com.furnisight.catalog.application.category.port.in.usecase.CreateCategoryUseCase;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import com.furnisight.catalog.domain.entities.Category;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import com.furnisight.catalog.domain.services.category.CategoryLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryService implements CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryLifecycleService categoryLifecycleService;

    @Override
    @Transactional
    @CacheEvict(value = {"categories_root", "categories_sub", "categories_all", "category_detail"}, allEntries = true)
    public void execute(CreateCategoryCommand command) {
        CategoryName name = new CategoryName(command.getName());
        CategorySlug slug = new CategorySlug(command.getSlug());

        Category category = categoryLifecycleService.createCategory(name, slug, command.getParentId());
        category.setIconUrl(command.getIconId());
        category.setVisible(command.getVisible() == null || command.getVisible());
        category.setDescription(command.getDescription());
        category.setImageUrl(command.getImageUrl());

        categoryRepository.save(category);
    }
}
