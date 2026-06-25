package com.furnisight.admin.catalog.product.web.dto.response;

public record ProductResponse(
        String id,
        String name,
        String sku,
        String category,
        double price,
        int stock,
        String status,
        String statusLabel,
        java.util.List<String> imageUrls,
        java.util.List<ProductVariantResponse> variants
) {
}
