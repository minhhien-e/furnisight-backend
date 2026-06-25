package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.CreateProductCommand;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.domain.entities.Product;
import com.furnisight.catalog.domain.entities.ProductImage;
import com.furnisight.catalog.domain.entities.ProductVariant;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;
import com.furnisight.catalog.domain.valueobjects.product.Price;
import com.furnisight.catalog.domain.valueobjects.product.ProductDescription;
import com.furnisight.catalog.domain.valueobjects.product.ProductDimensions;
import com.furnisight.catalog.domain.valueobjects.product.ProductName;
import com.furnisight.catalog.domain.valueobjects.product.ProductSlug;
import com.furnisight.catalog.domain.valueobjects.product.StockQuantity;
import lombok.RequiredArgsConstructor;
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
    public void execute(CreateProductCommand command) {
        ProductName name = new ProductName(command.getName());
        ProductSlug slug = new ProductSlug(command.getSlug());
        ProductDescription description = new ProductDescription(command.getDescription());

        List<ProductVariant> variants = new ArrayList<>();
        Set<String> skus = new HashSet<>();
        if (command.getVariants() != null) {
            for (CreateProductCommand.VariantCommand v : command.getVariants()) {
                String sku = normalizeSku(v.getSku());
                if (!skus.add(sku) || productRepository.findVariantIdBySku(sku).isPresent()) {
                    throw new IllegalArgumentException("Variant SKU already exists: " + sku);
                }
                ProductDimensions dims = new ProductDimensions(
                        v.getWeight(), v.getLength(), v.getWidth(), v.getHeight());
                variants.add(ProductVariant.builder()
                        .id(UUID.randomUUID())
                        .price(new Price(BigDecimal.valueOf(v.getPrice())))
                        .stockQuantity(new StockQuantity(v.getStockQuantity()))
                        .dimensions(dims)
                        .material(v.getMaterial())
                        .warranty(v.getWarranty())
                        .color(v.getColor())
                        .sku(sku)
                        .lowStockThreshold(validThreshold(v.getLowStockThreshold()))
                        .modelMediaId(v.getModelMediaId())
                        .modelUrl(v.getModelUrl())
                        .supports3d(v.getSupports3d() != null ? v.getSupports3d() : false)
                        .build());
            }
        }

        List<ProductImage> gallery = new ArrayList<>();
        if (command.getImageUrls() != null) {
            for (String imageUrl : command.getImageUrls()) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                gallery.add(ProductImage.builder()
                        .id(UUID.randomUUID())
                        .imageUrl(imageUrl.trim())
                        .position(gallery.size())
                        .build());
            }
        }

        Product product = productLifecycleService.createProduct(
                command.getCategoryId(),
                name,
                slug,
                command.getSku(),
                description,

                command.getFeatures(),
                gallery,
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

    private int validThreshold(Integer threshold) {
        int value = threshold == null || threshold == 0 ? 5 : threshold;
        if (value < 1 || value > 9999) {
            throw new IllegalArgumentException("Low stock threshold must be between 1 and 9999");
        }
        return value;
    }
}
