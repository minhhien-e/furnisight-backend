package com.furnisight.catalog.infrastructure.database.repository.impl.category;

import com.furnisight.catalog.domain.valueobjects.category.CategorySlug;
import com.furnisight.catalog.infrastructure.database.repository.jpa.CategoryJpaRepository;

import com.furnisight.catalog.domain.repository.CategoryRepository;
import com.furnisight.catalog.domain.entities.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaCategoryRepository;

    @Override
    public Optional<Category> findBySlug(CategorySlug slug) {
        return jpaCategoryRepository.findBySlug(slug);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaCategoryRepository.findById(id);
    }

    @Override
    public Category save(Category category) {
        return jpaCategoryRepository.save(category);
    }

    @Override
    public boolean existsByNameAndParentId(String name, UUID parentId){
        return jpaCategoryRepository.existsByNameValueAndParentId(name, parentId);
    }

    @Override
    public boolean existsSlug(CategorySlug slug) {
        return jpaCategoryRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByParentId(UUID parentId) {
        return jpaCategoryRepository.existsByParentId(parentId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaCategoryRepository.deleteById(id);
    }
}
