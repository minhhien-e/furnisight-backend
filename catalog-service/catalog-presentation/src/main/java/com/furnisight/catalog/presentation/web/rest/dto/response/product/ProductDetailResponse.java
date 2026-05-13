package com.furnisight.catalog.presentation.web.rest.dto.response.product;

import com.furnisight.catalog.application.product.dto.ProductDetailResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProductDetailResponse {
    private UUID id;
    private UUID shopId;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String status;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    private Map<String, String> attributes;
    private List<VariantResponse> variants;

    @Getter
    @Builder
    public static class VariantResponse {
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }

    public static ProductDetailResponse from(ProductDetailResponseDto dto) {
        return ProductDetailResponse.builder()
                .id(dto.getId())
                .shopId(dto.getShopId())
                .categoryId(dto.getCategoryId())
                .categoryName(dto.getCategoryName())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .weight(dto.getWeight())
                .length(dto.getLength())
                .height(dto.getHeight())
                .width(dto.getWidth())
                .attributes(dto.getAttributes())
                .variants(dto.getVariants().stream()
                        .map(v -> VariantResponse.builder()
                                .sku(v.getSku())
                                .price(v.getPrice())
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
