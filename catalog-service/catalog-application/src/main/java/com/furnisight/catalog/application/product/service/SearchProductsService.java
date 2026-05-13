package com.furnisight.catalog.application.product.service;

import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;
import com.furnisight.catalog.application.product.port.in.usecase.SearchProductsUseCase;
import com.furnisight.catalog.domain.entities.product.Product;
import com.furnisight.catalog.domain.repository.product.ProductRepository;
import com.furnisight.catalog.domain.enums.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProductsService implements SearchProductsUseCase {

    private final ProductRepository productRepository;

    @Override
    public List<ProductDetailResponseDto> execute(String query, UUID categoryId, String status, int page, int size) {
        List<Product> products = productRepository.search(query, categoryId, null, null, status, page, size);
        
        return products.stream()
                .map(product -> mapToDto(product))
                .collect(Collectors.toList());
    }

    private ProductDetailResponseDto mapToDto(Product product) {
        Map<String, String> attributes = Collections.emptyMap();
        if (product.getAttributes() != null) {
            attributes = product.getAttributes().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue() != null ? e.getValue().toString() : ""
                    ));
        }

        List<ProductDetailResponseDto.VariantDto> variants = Collections.emptyList();
        if (product.getVariants() != null) {
            variants = product.getVariants().stream()
                    .map(v -> ProductDetailResponseDto.VariantDto.builder()
                            .sku(v.getSku() != null ? v.getSku().getValue() : null)
                            .price(v.getPrice() != null ? v.getPrice().getValue().doubleValue() : 0.0)
                            .stockQuantity(v.getStockQuantity() != null ? v.getStockQuantity().getValue() : 0)
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductDetailResponseDto.builder()
                .id(product.getId())
                .shopId(product.getShopId())
                .categoryId(product.getCategoryId())
                .name(product.getName() != null ? product.getName().getValue() : null)
                .description(product.getDescription() != null ? product.getDescription().getValue() : null)
                .status(product.getProductStatus() != null ? product.getProductStatus().name() : null)
                .weight(product.getDimensions() != null ? product.getDimensions().getWeight() : null)
                .length(product.getDimensions() != null ? product.getDimensions().getLength() : null)
                .height(product.getDimensions() != null ? product.getDimensions().getHeight() : null)
                .width(product.getDimensions() != null ? product.getDimensions().getWidth() : null)
                .attributes(attributes)
                .variants(variants)
                .build();
    }
}
