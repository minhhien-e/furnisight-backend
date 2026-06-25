package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.UpdateProductInfoCommand;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductInfoUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import com.furnisight.catalog.domain.valueobjects.product.ProductDescription;
import com.furnisight.catalog.domain.valueobjects.product.ProductName;
import com.furnisight.catalog.domain.valueobjects.product.ProductSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductInfoService implements UpdateProductInfoUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;
    private final ProductUpdateEventService productUpdateEventService;

    @Override
    @Transactional
    public void execute(UpdateProductInfoCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productLifecycleService.updateProfile(
                product,
                command.getName() != null ? new ProductName(command.getName()) : null,
                command.getSlug() != null ? new ProductSlug(command.getSlug()) : null,
                command.getSku(),
                command.getDescription() != null ? new ProductDescription(command.getDescription()) : null,
                command.getFeatures());

        productRepository.save(product);
        productUpdateEventService.enqueue(product);
    }
}
