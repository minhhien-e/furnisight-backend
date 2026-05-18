package com.furnisight.catalog.domain.services.category;

import com.furnisight.catalog.domain.entities.category.Category;
import com.furnisight.catalog.domain.exceptions.*;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import com.furnisight.catalog.domain.valueobjects.category.CategoryName;
import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryLifecycleService {

    private final CategoryRepository categoryRepository;
    private final CategoryPathService pathService;

    public Category createCategory(CategoryName name, CategorySlug slug, UUID parentId) {
        // Buoc 1: Tinh path (dong thoi kiem tra parent ton tai neu co)
        String path = pathService.calculatePath(parentId, slug.getValue());

        // Buoc 2: Kiem tra slug trung (globally unique)
        if (categoryRepository.findBySlug(slug.getValue()).isPresent()) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_CATEGORY_SLUG);
        }

        // Buoc 3: Kiem tra name trung trong cung cap cha
        if (categoryRepository.existsByNameAndParentId(name.getValue(), parentId)) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        return Category.create(name, slug, parentId, path);
    }
}
