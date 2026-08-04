package com.furnisight.admin.catalog.category.web.dto.response;

public record CategoryResponse(
        String id,
        String name,
        String slug,
        int productCount,
        boolean visible,
        String visibleLabel,
        String createdAt,
        String iconId,
        String description,
        String imageUrl,
        String roomTypeId
) {
}
