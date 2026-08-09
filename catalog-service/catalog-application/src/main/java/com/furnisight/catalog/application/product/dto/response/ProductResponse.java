package com.furnisight.catalog.application.product.dto.response;

import com.furnisight.catalog.domain.valueobjects.product.VariantSpecifications;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements java.io.Serializable {
    private UUID id;
    private UUID shopId;
    private String name;
    private String description;
    private String status;
    private Double price;
    private String slug;
    private String sku;
    private String categoryName;
    private CategoryInfo category;
    private Integer stock;
    private Double rating;
    private Integer ratingCount;
    private Integer soldCount;
    private Boolean supports3d;
    private String image;
    private List<String> imageUrls;
    private List<String> gallery;
    private List<String> features;
    private List<Review> reviews;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String modelUrl;
    private UUID defaultVariantId;
    private String roomTypeHint;

    private List<VariantDto> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo implements java.io.Serializable {
        private String id;
        private String label;
        private String path;
        private String parentId;
        private String parentLabel;
        private String roomTypeId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review implements java.io.Serializable {
        private String id;
        private String user;
        private String avatar;
        private int rating;
        private String createdAt;
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto implements java.io.Serializable {
        private UUID id;
        private Double price;
        private Integer stockQuantity;
        private Double length; // cm
        private Double width; // cm
        private Double height; // cm
        private Double weight;
        private String material;
        private String color;
        private String warranty;
        private String sku;
        private Integer lowStockThreshold;
        private Boolean supports3d;
        private UUID modelMediaId;
        private String modelUrl;
        private List<String> imageUrls;
        private List<String> features;
        private VariantSpecifications specifications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStockDto implements java.io.Serializable {
        private UUID productId;
        private UUID variantId;
        private Integer stockQuantity;
    }
}
