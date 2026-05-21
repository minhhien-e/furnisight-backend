package com.furnisight.catalog.domain.services.product;

import com.furnisight.catalog.domain.entities.Category;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomTypeResolverService {

    private final CategoryRepository categoryRepository;

    public String resolve(UUID categoryId) {
        if (categoryId == null)
            return null;

        Category root = findRootCategory(categoryId);
        if (root == null || root.getName() == null)
            return null;

        return root.getName().getValue();
    }

    private Category findRootCategory(UUID categoryId) {
        Optional<Category> current = categoryRepository.findById(categoryId);
        int safeDepth = 0;
        while (current.isPresent() && safeDepth < 10) {
            Category cat = current.get();
            if (cat.getParentId() == null)
                return cat;
            current = categoryRepository.findById(cat.getParentId());
            safeDepth++;
        }
        return current.orElse(null);
    }
}
