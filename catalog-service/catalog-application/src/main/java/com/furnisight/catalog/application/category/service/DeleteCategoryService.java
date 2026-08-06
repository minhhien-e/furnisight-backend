package com.furnisight.catalog.application.category.service;

import com.furnisight.catalog.application.category.dto.command.DeleteCategoryCommand;
import com.furnisight.catalog.application.category.port.in.usecase.DeleteCategoryUseCase;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import com.furnisight.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "category_detail"}, allEntries = true)
    public void execute(DeleteCategoryCommand command) {
        if (categoryRepository.existsByParentId(command.categoryId())) {
            throw new IllegalArgumentException("Không thể xóa danh mục đang có danh mục con.");
        }

        if (productRepository.existsByCategoryId(command.categoryId())) {
            throw new IllegalArgumentException("Không thể xóa danh mục đang chứa sản phẩm.");
        }

        categoryRepository.deleteById(command.categoryId());
    }
}
