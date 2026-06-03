package com.furnisight.catalog.application.product.dto.projection;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDetailProjection {
    private UUID id;
    private UUID shopId;
    private String name;
    private String description;
    private String status;
    private Double price;
    private String slug;
    private CategoryInfo category;
    private Double oldPrice;
    private Double rating;
    private Integer ratingCount;
    private Integer soldCount;
    private List<String> tags;
    private Boolean supports3d;
    private String collection;
    private List<String> gallery;
    private List<String> features;
    private List<Review> reviews;
    private List<QA> qa;
    private String modelUrl;
    private String roomTypeHint;

    private List<VariantDto> variants;

    @Data
    @Builder
    public static class CategoryInfo {
        private String id;
        private String label;
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
    }
}
