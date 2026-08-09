package com.furnisight.promotion.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class ValidateComboCommand {
    private String userId;
    private String comboId;
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private String variantId;
        private Integer quantity;
        private Double price;
    }
}
