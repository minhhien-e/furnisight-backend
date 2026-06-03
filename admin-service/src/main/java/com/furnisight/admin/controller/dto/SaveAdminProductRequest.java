package com.furnisight.admin.controller.dto;

public record SaveAdminProductRequest(
        String name,
        String category,
        double price,
        int stock,
        String sku,
        String status,
        String statusLabel,
        String model3dUrl,
        String model3dFileName,
        long model3dSize,
        String description,
        java.util.List<String> imageUrls,
        java.util.List<SaveAdminProductVariantRequest> variants
) {
}
