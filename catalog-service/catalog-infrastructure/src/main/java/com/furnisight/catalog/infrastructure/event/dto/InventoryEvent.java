package com.furnisight.catalog.infrastructure.event.dto;

import java.util.List;
import java.util.UUID;

public record InventoryEvent(
        String orderCode,
        List<StockItem> items
) {
    public record StockItem(
            UUID productId,
            UUID variantId,
            int quantity
    ) {}
}
