package com.furnisight.promotion.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MarketingComboDto {
    private String id;
    private String name;
    private String description;
    private String discountType;
    private double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private List<String> placements;
    private List<Item> items;
    private int itemCount;
    private double originalAmount;
    private double finalAmount;
    private double savedAmount;
    private long usedCount;
    private String status;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class Item {
        private String productId;
        private String variantId;
        private String productName;
        private String sku;
        private String categoryName;
        private String image;
        private double price;
        private int quantity;
        private boolean snapshotMissing;
    }
}
