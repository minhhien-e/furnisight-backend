package com.furnisight.admin.controller.dto;

public record AdminProductResponse(
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
        java.util.List<AdminProductVariantResponse> variants
) {
}
