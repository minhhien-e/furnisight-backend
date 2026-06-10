package com.furnisight.admin.catalog.product.web.dto.response;

public record ProductVariantResponse(
        String id,
        String sku,
        double price,
        int stock,
        String color,
        String material,
        String warranty,
        double weight,
        double length,
        double width,
        double height,
        String label,
        int lowStockThreshold
) {
}
