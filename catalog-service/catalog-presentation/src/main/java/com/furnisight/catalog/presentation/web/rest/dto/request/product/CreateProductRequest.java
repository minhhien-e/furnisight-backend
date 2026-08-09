package com.furnisight.catalog.presentation.web.rest.dto.request.product;

import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    private UUID categoryId;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private Double basePrice;
    private VariantSpecifications specifications;
    private List<String> features;
    private List<String> imageUrls;

    private List<VariantRequest> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantRequest {
        private Double price;
        private Integer stockQuantity;
        private Double weight;
        private Double length;
        private Double width;
        private Double height;
        private String material;
        private String color;
        private String warranty;
        private String sku;
        private Integer lowStockThreshold;
        private Boolean supports3d;
        private UUID modelMediaId;
        private String modelUrl;
        private List<String> imageUrls;
        private VariantSpecifications specifications;
    }
}
