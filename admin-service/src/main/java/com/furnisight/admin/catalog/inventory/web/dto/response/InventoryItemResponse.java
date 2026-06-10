package com.furnisight.admin.catalog.inventory.web.dto.response;

public record InventoryItemResponse(
        String productId,
        String variantId,
        String sku,
        String name,
        String category,
        String variantLabel,
        int stock,
        int threshold,
        int stockPercent,
        String stockClass,
        String lastImport,
        String exportMonth,
        String status,
        String statusLabel
) {
}
