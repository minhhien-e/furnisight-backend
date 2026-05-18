package com.furnisight.catalog.domain.services.category;

import com.furnisight.catalog.domain.exceptions.*;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryPathService {

    private final CategoryRepository categoryRepository;

    /**
     * Tinh toan path cho Category dua vao parentId va slug.
     * - Neu la danh muc goc (parentId = null): path = "/slug"
     * - Neu co cha: path = parentPath + "/slug"
     */
    public String calculatePath(UUID parentId, String slug) {
        if (parentId == null) {
            return "/" + slug;
        }

        String parentPath = categoryRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND))
                .getPath();

        return parentPath + "/" + slug;
    }
}
