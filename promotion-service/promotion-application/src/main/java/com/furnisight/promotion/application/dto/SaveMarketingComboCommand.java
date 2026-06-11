package com.furnisight.promotion.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveMarketingComboCommand {
    private String name;
    private String description;
    private String discountType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private List<String> placements;
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private String variantId;
        private Integer quantity;
        private String productName;
        private String sku;
        private String categoryName;
        private String image;
        private Double price;
    }
}
