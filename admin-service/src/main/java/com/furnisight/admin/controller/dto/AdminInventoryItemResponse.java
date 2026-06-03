package com.furnisight.admin.controller.dto;

public record AdminInventoryItemResponse(
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
