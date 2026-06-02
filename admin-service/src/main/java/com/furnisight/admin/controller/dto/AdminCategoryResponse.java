package com.furnisight.admin.controller.dto;

public record AdminCategoryResponse(
        String id,
        String name,
        String slug,
        int productCount,
        boolean visible,
        String visibleLabel,
        String createdAt,
        String iconId,
        String description
) {
}
