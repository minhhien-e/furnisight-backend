package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.UpdateCategoryCommand;
import com.furnisight.catalog.application.category.port.in.usecase.UpdateCategoryUseCase;
import com.furnisight.catalog.domain.entities.category.Category;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import com.furnisight.catalog.domain.exceptions.CategoryNotFoundException;
import com.furnisight.catalog.domain.services.category.CategoryPathCalculator;
import com.furnisight.catalog.domain.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryService implements UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryPathCalculator pathCalculator;

    @Override
    @Transactional
    public void execute(UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(command.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException(command.getCategoryId()));

        CategoryName name = command.getName() != null ? new CategoryName(command.getName()) : null;
        CategorySlug slug = command.getSlug() != null ? new CategorySlug(command.getSlug()) : null;

        // Tinh path moi dua vao slug moi (neu co) hoac slug hien tai
        String slugValue = slug != null ? slug.getValue() : category.getSlug().getValue();
        String path = pathCalculator.calculate(command.getParentId(), slugValue);

        category.update(name, slug, command.getParentId(), path);

        categoryRepository.save(category);
    }
}
