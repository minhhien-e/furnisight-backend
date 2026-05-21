package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.UpdateProductStatusCommand;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductStatusUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.exceptions.*;
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
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        switch (command.getStatus()) {
            case ACTIVE -> product.activate();
            case INACTIVE -> product.deactivate();
            default -> throw new IllegalArgumentException("Invalid product status: " + command.getStatus());
        }

        productRepository.save(product);
    }
}
