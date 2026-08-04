package com.furnisight.admin.catalog.product.web.dto.request;

public record UpsertProductVariantRequest(
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
        int lowStockThreshold,
        String modelMediaId,
        String modelUrl,
        boolean supports3d,
        java.util.List<String> imageUrls,
        String specifications
) {
}
