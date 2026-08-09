package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.ChangeProductCategoryCommand;
import com.furnisight.catalog.application.product.port.in.usecase.ChangeProductCategoryUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.exceptions.*;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class ChangeProductCategoryService implements ChangeProductCategoryUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;
    private final ProductUpdateEventService productUpdateEventService;

    @Override
    @Transactional
    @CacheEvict(value = {"top_products", "product_detail"}, allEntries = true)
    public void execute(ChangeProductCategoryCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productLifecycleService.changeCategory(product, command.getCategoryId());

        productRepository.save(product);
        productUpdateEventService.enqueue(product);
    }
}
