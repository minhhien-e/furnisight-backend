package com.furnisight.catalog.application.product.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
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
    private List<String> tags;
    private Boolean supports3d;
    private String image;
    private List<String> imageUrls;
    private List<String> gallery;
    private List<String> features;
    private List<Review> reviews;
    private List<QA> qa;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String modelUrl;
    private UUID defaultVariantId;
    private String roomTypeHint;

    private List<VariantDto> variants;

    @Data
    @Builder
    public static class CategoryInfo {
        private String id;
        private String label;
        private String path;
        private String parentId;
        private String parentLabel;
    }

    @Data
    @Builder
    public static class Review {
        private String id;
        private String user;
        private String avatar;
        private int rating;
        private String createdAt;
        private String comment;
    }

    @Data
    @Builder
    public static class QA {
        private String id;
        private String question;
        private String answer;
        private String asker;
        private String date;
    }

    @Data
    @Builder
    public static class VariantDto {
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
    }
}
