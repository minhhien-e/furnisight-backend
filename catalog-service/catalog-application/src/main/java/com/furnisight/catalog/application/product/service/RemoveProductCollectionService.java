package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.RemoveProductCollectionCommand;
import com.furnisight.catalog.application.product.port.in.usecase.RemoveProductCollectionUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.exceptions.*;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveProductCollectionService implements RemoveProductCollectionUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;

    @Override
    @Transactional
    public void execute(RemoveProductCollectionCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productLifecycleService.removeFromCollection(product);

        productRepository.save(product);
    }
}
