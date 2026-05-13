package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.CreateCategoryCommand;
import com.furnisight.catalog.application.category.port.in.usecase.CreateCategoryUseCase;
import com.furnisight.catalog.domain.repository.category.CategoryRepository;
import com.furnisight.catalog.domain.entities.category.Category;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import com.furnisight.catalog.domain.services.category.CategoryLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryService implements CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryLifecycleService categoryLifecycleService;

    @Override
    @Transactional
    public void execute(CreateCategoryCommand command) {
        CategoryName name = new CategoryName(command.getName());
        CategorySlug slug = new CategorySlug(command.getSlug());

        Category category = categoryLifecycleService.createCategory(name, slug, command.getParentId());

        categoryRepository.save(category);
    }
}
