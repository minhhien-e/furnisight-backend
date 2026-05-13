package com.furnisight.catalog.application.category.helper;

import com.furnisight.catalog.domain.repository.category.CategoryRepository;
import com.furnisight.catalog.domain.exceptions.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryPathCalculator {

    private final CategoryRepository categoryRepository;

    /**
     * Tinh toan path cho Category dua vao parentId va slug.
     * - Neu la danh muc goc (parentId = null): path = "/slug"
     * - Neu co cha: path = parentPath + "/slug"
     *
     * @param parentId UUID cua category cha, null neu la danh muc goc
     * @param slug     slug cua category hien tai
     * @return path da tinh toan
     */
    public String calculate(UUID parentId, String slug) {
        if (parentId == null) {
            return "/" + slug;
        }

        String parentPath = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException(parentId))
                .getPath();

        return parentPath + "/" + slug;
    }
}
