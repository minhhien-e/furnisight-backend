package com.furnisight.admin.catalog.product.web.dto.request;

public record UpsertProductRequest(
        String name,
        String category,
        double price,
        int stock,
        String sku,
        String status,
        String statusLabel,
        String description,
        java.util.List<String> imageUrls,
        java.util.List<UpsertProductVariantRequest> variants
) {
}
