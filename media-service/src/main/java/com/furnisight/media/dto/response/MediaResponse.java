package com.furnisight.media.dto.response;

import com.furnisight.media.entity.MediaAsset;
import com.furnisight.media.enums.AssetState;
import com.furnisight.media.enums.MediaType;
import com.furnisight.media.enums.OwnerType;

import java.util.UUID;

public record MediaResponse(
    UUID id,
    String url,
    String secureUrl,
    String originalFilename,
    String mimeType,
    MediaType mediaType,
    OwnerType ownerType,
    UUID ownerId,
    AssetState state,
    Long sizeBytes,
    String format,
    Integer width,
    Integer height
) {
    public static MediaResponse from(MediaAsset asset) {
        return new MediaResponse(
            asset.getId(),
            asset.getUrl(),
            asset.getSecureUrl(),
            asset.getOriginalFilename(),
            asset.getMimeType(),
            asset.getMediaType(),
            asset.getOwnerType(),
            asset.getOwnerId(),
            asset.getState(),
            asset.getSizeBytes(),
            asset.getFormat(),
            asset.getWidth(),
            asset.getHeight()
        );
    }
}
