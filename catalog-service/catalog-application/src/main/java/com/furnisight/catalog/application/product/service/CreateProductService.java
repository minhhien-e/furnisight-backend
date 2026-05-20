package com.furnisight.catalog.application.product.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furnisight.catalog.application.product.dto.command.CreateProductCommand;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.domain.entities.Product;
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
        if (command.getVariants() != null) {
            for (CreateProductCommand.VariantCommand v : command.getVariants()) {
                ProductDimensions dims = new ProductDimensions(v.getWeight(), v.getLength(), v.getWidth(),
                        v.getHeight());
                variants.add(ProductVariant.builder()
                        .id(UUID.randomUUID())
                        .price(new Price(java.math.BigDecimal.valueOf(v.getPrice())))
                        .stockQuantity(new StockQuantity(v.getStockQuantity()))
                        .dimensions(dims)
                        .build());
            }
        }

        Product product = productLifecycleService.createProduct(
                command.getCategoryId(),
                null, // collectionId
                name,
                slug,
                description,
                command.getAttributes(),
                new HashMap<>(), // metadata
                new HashMap<>(), // specs
                new ArrayList<>(), // gallery
                variants);

        productRepository.save(product);
    }
}
