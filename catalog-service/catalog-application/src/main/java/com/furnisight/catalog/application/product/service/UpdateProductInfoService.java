package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.UpdateProductInfoCommand;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductInfoUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.product.Product;
import com.furnisight.catalog.domain.valueobjects.product.*;
import com.furnisight.catalog.domain.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductInfoService implements UpdateProductInfoUseCase {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void execute(UpdateProductInfoCommand command) {
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        product.verifyOwnership(command.getShopId());

        product.updateInfo(
            command.getName() != null ? new ProductName(command.getName()) : null,
            command.getDescription() != null ? new ProductDescription(command.getDescription()) : null,
            (command.getWeight() != null && command.getLength() != null && command.getWidth() != null && command.getHeight() != null) ?
                new ProductDimensions(command.getWeight(), command.getLength(), command.getWidth(), command.getHeight()) : null,
            command.getAttributes()
        );

        productRepository.save(product);
    }
}
