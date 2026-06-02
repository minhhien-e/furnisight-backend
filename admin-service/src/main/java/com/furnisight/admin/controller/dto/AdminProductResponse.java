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
        String model3dUrl,
        String model3dFileName,
        long model3dSize,
        java.util.List<String> imageUrls
) {
}
