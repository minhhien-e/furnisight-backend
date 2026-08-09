package com.furnisight.admin.catalog.category.web.dto.request;

public record UpsertCategoryRequest(
        String name,
        String slug,
        String iconId,
        boolean visible,
        String description,
        String imageUrl,
        String roomTypeId
) {
}
