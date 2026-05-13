package com.furnisight.catalog.presentation.web.rest.controller;

import com.furnisight.catalog.application.product.dto.GetProductDetailQuery;
import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;
import com.furnisight.catalog.application.product.port.in.usecase.GetProductDetailQueryUseCase;
import com.furnisight.catalog.application.product.port.in.usecase.SearchProductsUseCase;
import com.furnisight.catalog.presentation.web.rest.dto.response.product.ProductDetailResponse;
import com.furnisight.catalog.presentation.web.rest.dto.response.product.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {
    private final GetProductDetailQueryUseCase getProductDetailQueryUseCase;
    private final SearchProductsUseCase searchProductsUseCase;

    @GetMapping
    public ResponseEntity<ProductListResponse> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        var results = searchProductsUseCase.execute(query, categoryId, status, page, size);
        
        var response = ProductListResponse.builder()
                .products(results.stream()
                        .map(dto -> ProductListResponse.ProductItemResponse.builder()
                                .id(dto.getId())
                                .name(dto.getName())
                                .categoryName(dto.getCategoryName())
                                .price(!dto.getVariants().isEmpty() ? dto.getVariants().get(0).getPrice() : 0.0)
                                .build())
                        .collect(Collectors.toList()))
                .total(results.size())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable UUID productId){
        GetProductDetailQuery query = new GetProductDetailQuery(productId);
        ProductDetailResponseDto responseDto = getProductDetailQueryUseCase.execute(query);
        return ResponseEntity.ok(ProductDetailResponse.from(responseDto));
    }
}
