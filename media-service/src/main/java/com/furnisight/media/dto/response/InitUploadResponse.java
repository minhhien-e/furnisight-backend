package com.furnisight.media.dto.response;

import java.util.UUID;

public record InitUploadResponse(
    UUID mediaId,
    String uploadUrl,
    String state,
    CloudinaryUploadFields fields
) {
}
