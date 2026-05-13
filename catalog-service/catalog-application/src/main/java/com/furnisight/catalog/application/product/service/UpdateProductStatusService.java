package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.UpdateProductStatusCommand;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductStatusUseCase;
import com.furnisight.catalog.domain.repository.product.ProductRepository;
import com.furnisight.catalog.domain.entities.product.Product;
import com.furnisight.catalog.domain.exceptions.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductStatusService implements UpdateProductStatusUseCase {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void execute(UpdateProductStatusCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));

        product.verifyOwnership(command.getShopId());

        switch (command.getStatus()) {
            case ACTIVE:
                product.activate();
                break;
            case DELETED:
                product.markAsDeleted();
                break;
            default:
                product.setProductStatus(command.getStatus());
        }

        productRepository.save(product);
    }
}

