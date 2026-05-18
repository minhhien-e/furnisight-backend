package com.furnisight.media.core.repository;

import com.furnisight.media.core.model.entity.MediaAsset;
import java.util.UUID;

import com.furnisight.media.core.model.enums.AssetState;
import java.time.LocalDateTime;
import java.util.List;

public interface MediaAssetRepository {
    MediaAsset findById(UUID id);
    MediaAsset save(MediaAsset mediaAsset);
    List<MediaAsset> findByStateAndCreatedAtBefore(AssetState state, LocalDateTime time);
    void delete(MediaAsset mediaAsset);
}
