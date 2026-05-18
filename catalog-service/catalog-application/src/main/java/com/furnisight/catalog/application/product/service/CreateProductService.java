package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.command.CreateProductCommand;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.domain.repository.ProductRepository;
import com.furnisight.catalog.domain.entities.product.Product;
import com.furnisight.catalog.domain.valueobjects.product.*;
import com.furnisight.catalog.domain.services.product.ProductLifecycleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductLifecycleService productLifecycleService;

    @Override
    @Transactional
    public void execute(CreateProductCommand command) {
        ProductName name = new ProductName(command.getName());
        ProductDescription description = new ProductDescription(command.getDescription());
        ProductDimensions dimensions = new ProductDimensions(
                command.getWeight(), command.getLength(), command.getWidth(), command.getHeight());

        List<ProductLifecycleService.VariantRequest> variantRequests = new ArrayList<>();
        if (command.getVariants() != null) {
            for (CreateProductCommand.VariantCommand v : command.getVariants()) {
                variantRequests.add(ProductLifecycleService.VariantRequest.builder()
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .stockQuantity(v.getStockQuantity())
                        .build());
            }
        }

        Product product = productLifecycleService.createProduct(
                command.getShopId(), command.getCategoryId(), name, description, dimensions, 
                command.getAttributes(), variantRequests);

        productRepository.save(product);
    }
}
