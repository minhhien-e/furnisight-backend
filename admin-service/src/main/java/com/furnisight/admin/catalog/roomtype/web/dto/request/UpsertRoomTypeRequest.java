package com.furnisight.admin.catalog.roomtype.web.dto.request;

public record UpsertRoomTypeRequest(
        String name,
        String slug,
        String description,
        boolean visible,
        String imageUrl,
        String mediaId
) {
}
