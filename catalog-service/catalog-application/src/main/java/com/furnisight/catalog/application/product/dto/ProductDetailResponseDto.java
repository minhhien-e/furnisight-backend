package com.furnisight.catalog.application.product.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ProductDetailResponseDto {
    private UUID id;
    private UUID shopId;
    private UUID categoryId;
    private String categoryName; // Join tu bang categories khi query
    private String name;
    private String description;
    private String status;
    private Double weight;
    private Double length;
    private Double height;
    private Double width;
    private Map<String, String> attributes;
    private List<VariantDto> variants;

    @Data
    @Builder
    public static class VariantDto{
        private String sku;
        private Double price;
        private Integer stockQuantity;
    }
}
