package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.CreateProductCommand;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.entities.ProductVariantImage;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;

    @Override
    @Transactional
    @CacheEvict(value = {"top_products", "product_detail"}, allEntries = true)
    public void execute(CreateProductCommand command) {
        List<ProductVariant> variants = new ArrayList<>();
        Set<String> skus = new HashSet<>();
        if (command.getVariants() != null) {
            for (CreateProductCommand.VariantCommand v : command.getVariants()) {
                String sku = normalizeSku(v.getSku());
                if (!skus.add(sku) || productRepository.findVariantIdBySku(sku).isPresent()) {
                    throw new IllegalArgumentException("Variant SKU already exists: " + sku);
                }
                ProductDimensions dims = null;
                if (v.getWeight() != null && v.getLength() != null && v.getWidth() != null && v.getHeight() != null) {
                    dims = new ProductDimensions(v.getWeight(), v.getLength(), v.getWidth(), v.getHeight());
                }
                ProductVariant variant = productLifecycleService.createVariant(
                        v.getSku(),
                        v.getLowStockThreshold(),
                        new Price(BigDecimal.valueOf(v.getPrice())),
                        new StockQuantity(v.getStockQuantity()),
                        dims,
                        v.getMaterial(),
                        v.getWarranty(),
                        v.getColor(),
                        v.getModelMediaId(),
                        v.getModelUrl(),
                        v.getSupports3d(),
                        v.getImageUrls()
                );
                variants.add(variant);
            }
        }

        ProductDimensions productDims = new ProductDimensions(
                command.getWeight(), command.getLength(), command.getWidth(), command.getHeight());

        Product product = productLifecycleService.createProduct(
                command.getCategoryId(),
                command.getName(),
                command.getSlug(),
                command.getSku(),
                command.getDescription(),
                command.getFeatures(),
                command.getImageUrl(),
                command.getImageMediaId(),
                command.getColor(),
                productDims,
                variants);

        productRepository.save(product);
    }

    private String normalizeSku(String sku) {
        String normalized = sku == null ? "" : sku.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Variant SKU is required");
        }
        return normalized;
    }


}
