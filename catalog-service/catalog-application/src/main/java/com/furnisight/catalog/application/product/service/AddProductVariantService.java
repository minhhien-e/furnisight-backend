package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.AddProductVariantCommand;
import com.furnisight.catalog.application.product.port.in.usecase.AddProductVariantUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.entities.ProductVariantImage;
import com.furnisight.catalog.domain.exceptions.ErrorCode;
import com.furnisight.catalog.domain.exceptions.NotFoundException;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddProductVariantService implements AddProductVariantUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;
    private final ProductUpdateEventService productUpdateEventService;

    @Override
    @Transactional
    public void execute(AddProductVariantCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductDimensions dims = new ProductDimensions(
                command.getWeight(),
                command.getLength(),
                command.getWidth(),
                command.getHeight());

        ProductVariant variant = productLifecycleService.createVariant(
                command.getSku(),
                command.getLowStockThreshold(),
                new Price(BigDecimal.valueOf(command.getPrice())),
                new StockQuantity(command.getStockQuantity()),
                dims,
                command.getMaterial(),
                command.getWarranty(),
                command.getColor(),
                command.getModelMediaId(),
                command.getModelUrl(),
                command.getSupports3d(),
                command.getImageUrls(),
                command.getSpecifications()
        );

        productLifecycleService.addVariant(product, variant);

        productRepository.save(product);
        productUpdateEventService.enqueue(product);
    }
}
