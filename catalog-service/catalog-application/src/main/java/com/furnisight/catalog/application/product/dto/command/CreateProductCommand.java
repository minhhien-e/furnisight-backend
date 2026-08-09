package com.furnisight.catalog.application.product.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductCommand {
    private UUID categoryId;
    private String name;
    private String slug;
    private String sku;
    private String description;

    private List<String> features;
    private String imageUrl;
    private UUID imageMediaId;
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private String color;
    private List<VariantCommand> variants;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantCommand {
        private Double price;
        private Integer stockQuantity;
        private Double weight;
        private Double length;   // cm, optional
        private Double width;    // cm, optional
        private Double height;   // cm, optional
        private String material; // required
        private String warranty; // optional
        private String color;
        private String sku;
        private Integer lowStockThreshold;
        private UUID modelMediaId;
        private String modelUrl;
        private Boolean supports3d;
        private List<String> imageUrls;
        private com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications specifications;
    }
}
