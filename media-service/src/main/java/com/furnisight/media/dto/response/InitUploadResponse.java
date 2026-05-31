package com.furnisight.media.dto.response;

import java.util.Map;
import java.util.UUID;

public record InitUploadResponse(
    UUID mediaId,
    String uploadUrl,
    String state,
    Map<String, Object> fields
) {
}
