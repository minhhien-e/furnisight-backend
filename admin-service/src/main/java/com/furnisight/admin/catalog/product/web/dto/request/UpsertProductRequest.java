package com.furnisight.admin.catalog.product.web.dto.request;

public record UpsertProductRequest(
        String name,
        String category,
        double price,
        int stock,
        String sku,
        String status,
        String statusLabel,
        String modelMediaId,
        String modelUrl,
        boolean supports3d,
        String description,
        java.util.List<String> imageUrls,
        java.util.List<UpsertProductVariantRequest> variants
) {
}
