package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.AssignProductCollectionCommand;
import com.furnisight.catalog.application.product.port.in.usecase.AssignProductCollectionUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.exceptions.*;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignProductCollectionService implements AssignProductCollectionUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;

    @Override
    @Transactional
    public void execute(AssignProductCollectionCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productLifecycleService.assignToCollection(product, command.getCollectionId());

        productRepository.save(product);
    }
}
