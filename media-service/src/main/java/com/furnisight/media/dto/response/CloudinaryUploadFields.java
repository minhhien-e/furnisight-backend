package com.furnisight.media.dto.response;

public record CloudinaryUploadFields(
        String apiKey,
        long timestamp,
        String publicId,
        String folder,
        String signature
) {
}
