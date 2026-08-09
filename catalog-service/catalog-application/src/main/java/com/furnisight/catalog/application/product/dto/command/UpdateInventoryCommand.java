package com.furnisight.catalog.application.product.dto.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UpdateInventoryCommand {
    private String orderCode;
    private List<StockItem> items;

    @Data
    @Builder
    public static class StockItem {
        private UUID productId;
        private UUID variantId;
        private int quantity;
    }
}
