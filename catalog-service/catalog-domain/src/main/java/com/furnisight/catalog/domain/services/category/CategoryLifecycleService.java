package com.furnisight.catalog.domain.services.category;

import com.furnisight.catalog.domain.entities.category.Category;
import com.furnisight.catalog.domain.exceptions.DuplicateCategoryNameException;
import com.furnisight.catalog.domain.exceptions.DuplicateCategorySlugException;
import com.furnisight.catalog.domain.repository.category.CategoryRepository;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryLifecycleService {

    private final CategoryRepository categoryRepository;
    private final CategoryPathCalculator pathCalculator;

    public Category createCategory(CategoryName name, CategorySlug slug, UUID parentId) {
        // Buoc 1: Tinh path (dong thoi kiem tra parent ton tai neu co)
        String path = pathCalculator.calculate(parentId, slug.getValue());

        // Buoc 2: Kiem tra slug trung (globally unique)
        if (categoryRepository.findBySlug(slug.getValue()).isPresent()) {
            throw new DuplicateCategorySlugException(slug.getValue());
        }

        // Buoc 3: Kiem tra name trung trong cung cap cha
        if (categoryRepository.existsByNameAndParentId(name.getValue(), parentId)) {
            throw new DuplicateCategoryNameException(name.getValue(), parentId);
        }

        return Category.create(name, slug, parentId, path);
    }
}
