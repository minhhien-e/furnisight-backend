package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.product.dto.CreateProductCommand;
import com.furnisight.catalog.application.product.dto.UpdateProductInfoCommand;
import com.furnisight.catalog.application.product.dto.UpdateProductVariantsCommand;
import com.furnisight.catalog.application.product.dto.UpdateProductStatusCommand;
import com.furnisight.catalog.application.product.port.in.usecase.CreateProductUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductInfoUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductVariantsUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductStatusUseCase;
import com.furnisight.catalog.presentation.web.rest.dto.request.product.CreateProductRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.product.UpdateProductInfoRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.product.UpdateProductStatusRequest;
import com.furnisight.catalog.presentation.web.rest.dto.request.product.UpdateProductVariantsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductInfoUseCase updateProductInfoUseCase;
    private final UpdateProductVariantsUseCase updateProductVariantsUseCase;
    private final UpdateProductStatusUseCase updateProductStatusUseCase;

    @PostMapping()
    public ResponseEntity<Void> createProduct(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = CreateProductCommand.builder()
                .shopId(request.getShopId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .description(request.getDescription())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .attributes(request.getAttributes())
                .variants(request.getVariants().stream()
                        .map(v -> CreateProductCommand.VariantCommand.builder()
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        createProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{productId}/info")
    public ResponseEntity<Void> updateProductInfo(@PathVariable UUID productId, @RequestBody UpdateProductInfoRequest request) {
        UpdateProductInfoCommand command = UpdateProductInfoCommand.builder()
                .productId(productId)
                .shopId(request.getShopId())
                .name(request.getName())
                .description(request.getDescription())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .attributes(request.getAttributes())
                .build();

        updateProductInfoUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}/variants")
    public ResponseEntity<Void> updateProductVariants(@PathVariable UUID productId, @RequestBody UpdateProductVariantsRequest request) {
        UpdateProductVariantsCommand command = UpdateProductVariantsCommand.builder()
                .productId(productId)
                .shopId(request.getShopId())
                .variants(request.getVariants().stream()
                        .map(v -> UpdateProductVariantsCommand.VariantCommand.builder()
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        updateProductVariantsUseCase.execute(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable UUID productId, @RequestBody UpdateProductStatusRequest request) {
        UpdateProductStatusCommand command = UpdateProductStatusCommand.builder()
                .productId(productId)
                .shopId(request.getShopId())
                .status(request.getStatus())
                .build();

        updateProductStatusUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}
