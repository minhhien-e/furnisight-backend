package com.furnisight.admin.controller.dto;

public record SaveAdminCategoryRequest(
        String name,
        String slug,
        String iconId,
        boolean visible,
        String description,
        String imageUrl
) {
}
