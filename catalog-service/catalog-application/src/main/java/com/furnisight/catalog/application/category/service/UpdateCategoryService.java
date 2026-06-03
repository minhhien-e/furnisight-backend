package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.command.UpdateCategoryCommand;
import com.furnisight.catalog.application.category.port.in.usecase.UpdateCategoryUseCase;
import com.furnisight.catalog.domain.entities.Category;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryService implements UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void execute(UpdateCategoryCommand command) {
        Category category = categoryRepository.findById(command.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        CategoryName name = new CategoryName(command.getName());
        CategorySlug slug = new CategorySlug(command.getSlug());

        category.update(name, slug, command.getParentId());
        category.setIconUrl(command.getIconId());
        category.setVisible(command.getVisible() == null || command.getVisible());
        category.setDescription(command.getDescription());
        category.setImageUrl(command.getImageUrl());

        categoryRepository.save(category);
    }
}
