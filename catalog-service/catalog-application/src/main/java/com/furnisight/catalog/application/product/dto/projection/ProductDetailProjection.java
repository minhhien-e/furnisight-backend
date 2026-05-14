package com.furnisight.catalog.application.product.dto.projection;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ProductDetailProjection {
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
    private Double price; // Added for FE compatibility
    private String image; // Added for FE compatibility
    private String slug;
    private CategoryInfo category;
    private String thumbnailUrl;
    private Double oldPrice;
    private Double rating;
    private Integer ratingCount;
    private Integer stock;
    private List<String> tags;
    private List<String> materials;
    private List<String> colors;
    private List<String> sizes; // newly added
    private Boolean supports3d;
    private List<Breadcrumb> breadcrumb;
    private String collection;
    private List<String> gallery;
    private List<String> features;
    private Map<String, String> specs;
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
    public static class Breadcrumb {
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
        private String date;
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
        private String sku;
        private Double price;
        private Integer stockQuantity;
        // Per-variant attributes
        private String color;
        private String colorLabel;
        private String material;
        private String materialLabel;
        private String size;
    }
}
