package com.furnisight.catalog.domain.services.category;

import com.furnisight.catalog.domain.entities.Category;
import com.furnisight.catalog.domain.exceptions.AlreadyExistsException;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
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

    public Category createCategory(CategoryName name, CategorySlug slug, UUID parentId) {

        if (categoryRepository.existsSlug(slug)) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_CATEGORY_SLUG);
        }

        if (categoryRepository.existsByNameAndParentId(name.getValue(), parentId)) {
            throw new AlreadyExistsException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        return Category.create(name, slug, parentId);
    }

    public void updateCategory(Category category, CategoryName name, CategorySlug slug, UUID parentId) {
        if (slug != null && !slug.equals(category.getSlug())) {
            if (categoryRepository.existsSlug(slug)) {
                throw new AlreadyExistsException(ErrorCode.DUPLICATE_CATEGORY_SLUG);
            }
        }

        boolean nameChanged = name != null && !name.equals(category.getName());
        boolean parentChanged = (parentId == null && category.getParentId() != null)
                || (parentId != null && !parentId.equals(category.getParentId()));

        if (nameChanged || parentChanged) {
            String checkName = name != null ? name.getValue() : category.getName().getValue();
            if (categoryRepository.existsByNameAndParentId(checkName, parentId)) {
                throw new AlreadyExistsException(ErrorCode.DUPLICATE_CATEGORY_NAME);
            }
        }

        category.update(name, slug, parentId);
    }
}
