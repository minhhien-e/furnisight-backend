package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.AddProductVariantCommand;
import com.furnisight.catalog.application.product.port.in.usecase.AddProductVariantUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductVariant;
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
import java.util.Locale;

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

        String sku = command.getSku() == null ? "" : command.getSku().trim().toUpperCase(Locale.ROOT);
        if (sku.isBlank()) {
            throw new IllegalArgumentException("Variant SKU is required");
        }
        if (productRepository.findVariantIdBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Variant SKU already exists: " + sku);
        }
        int threshold = command.getLowStockThreshold() == null || command.getLowStockThreshold() == 0
                ? 5 : command.getLowStockThreshold();
        if (threshold < 1 || threshold > 9999) {
            throw new IllegalArgumentException("Low stock threshold must be between 1 and 9999");
        }

        ProductVariant variant = ProductVariant.builder()
                .id(UUID.randomUUID())
                .price(new Price(BigDecimal.valueOf(command.getPrice())))
                .stockQuantity(new StockQuantity(command.getStockQuantity()))
                .dimensions(dims)
                .material(command.getMaterial())
                .warranty(command.getWarranty())
                .color(command.getColor())
                .sku(sku)
                .lowStockThreshold(threshold)
                .build();

        productLifecycleService.addVariant(product, variant);

        productRepository.save(product);
        productUpdateEventService.enqueue(product);
    }
}
