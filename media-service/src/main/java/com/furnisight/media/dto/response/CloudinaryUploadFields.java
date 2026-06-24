package com.furnisight.media.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CloudinaryUploadFields(
        @JsonProperty("api_key") String apiKey,
        long timestamp,
        @JsonProperty("public_id") String publicId,
        String folder,
        String signature
) {
}
