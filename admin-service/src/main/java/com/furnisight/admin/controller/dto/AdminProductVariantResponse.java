package com.furnisight.admin.controller.dto;

public record AdminProductVariantResponse(
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
        String label
) {
}
