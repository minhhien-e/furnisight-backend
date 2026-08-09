package com.furnisight.admin.catalog.roomtype.web.dto.response;

public record RoomTypeResponse(
        String id,
        String name,
        String slug,
        String description,
        boolean visible,
        String createdAt,
        String updatedAt,
        String imageUrl,
        String mediaId
) {
}
