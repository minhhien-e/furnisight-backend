package com.furnisight.promotion.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SaveMarketingComboCommand {
    private String name;
    private String description;
    private UUID imageMediaId;
    private String imageUrl;
    private String discountType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private String variantId;
        private String productSlug;
        private Integer quantity;
        private String productName;
        private String sku;
        private String categoryName;
        private String image;
        private Double price;
    }
}
