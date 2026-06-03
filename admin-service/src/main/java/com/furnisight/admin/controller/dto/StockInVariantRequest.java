package com.furnisight.admin.controller.dto;

public record StockInVariantRequest(
        String productId,
        String variantId,
        int quantity,
        String note
) {
}
