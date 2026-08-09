package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.RemoveProductVariantCommand;
import com.furnisight.catalog.application.product.port.in.usecase.RemoveProductVariantUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveProductVariantService implements RemoveProductVariantUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;
    private final ProductUpdateEventService productUpdateEventService;

    @Override
    @Transactional
    public void execute(RemoveProductVariantCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        productLifecycleService.removeVariant(product, command.getVariantId());

        productRepository.save(product);
        productUpdateEventService.enqueue(product);
    }
}
