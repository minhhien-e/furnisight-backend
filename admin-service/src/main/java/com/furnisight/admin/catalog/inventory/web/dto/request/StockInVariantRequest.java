package com.furnisight.admin.catalog.inventory.web.dto.request;

public record StockInVariantRequest(
        String productId,
        String variantId,
        int quantity,
        String note
) {
}
