package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MarketingComboDto {
    private String id;
    private String name;
    private String description;
    private UUID imageMediaId;
    private String imageUrl;
    private String discountType;
    private double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private List<Item> items;
    private int itemCount;
    private double originalAmount;
    private double finalAmount;
    private double savedAmount;
    private long usedCount;
    private String status;
    private boolean available;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class Item {
        private String productId;
        private String variantId;
        private String productSlug;
        private String productName;
        private String sku;
        private String categoryName;
        private String image;
        private double price;
        private int quantity;
        private boolean snapshotMissing;
        private Integer stockQuantity;
        private boolean available;
    }
}
