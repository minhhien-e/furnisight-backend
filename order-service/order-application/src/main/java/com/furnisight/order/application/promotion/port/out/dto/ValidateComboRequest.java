package com.furnisight.order.application.promotion.port.out.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ValidateComboRequest {
    private UUID userId;
    private String comboId;
    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        private String productId;
        private String variantId;
        private Integer quantity;
        private Double price;
    }
}
