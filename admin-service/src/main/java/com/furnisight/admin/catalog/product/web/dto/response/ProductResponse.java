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
        String modelMediaId,
        String modelUrl,
        boolean supports3d,
        String modelFileName,
        long modelFileSize,
        java.util.List<String> imageUrls,
        java.util.List<ProductVariantResponse> variants
) {
}
