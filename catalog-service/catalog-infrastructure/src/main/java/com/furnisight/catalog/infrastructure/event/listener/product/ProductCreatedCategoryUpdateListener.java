package com.furnisight.catalog.infrastructure.event.listener.product;

import com.furnisight.catalog.domain.entities.category.Category;
import com.furnisight.catalog.domain.events.product.ProductCreatedEvent;
import com.furnisight.catalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedCategoryUpdateListener {

    private final CategoryRepository categoryRepository;

    @EventListener
    @Transactional
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Handling ProductCreatedEvent for categoryId: {}", event.getCategoryId());
        updateProductCountRecursively(event.getCategoryId());
    }

    private void updateProductCountRecursively(UUID categoryId) {
        if (categoryId == null) {
            return;
        }

        categoryRepository.findById(categoryId).ifPresent(category -> {
            category.incrementProductCount();
            categoryRepository.save(category);
            log.debug("Incremented product count for category: {} (id: {}). New count: {}", 
                category.getName().getValue(), category.getId(), category.getProductCount());
            
            // Recursive call for parent
            if (category.getParentId() != null) {
                updateProductCountRecursively(category.getParentId());
            }
        });
    }
}
