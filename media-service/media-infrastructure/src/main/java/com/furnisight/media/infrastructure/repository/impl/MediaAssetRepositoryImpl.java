package com.furnisight.media.infrastructure.repository.impl;

import com.furnisight.media.core.exception.MediaNotFoundException;
import com.furnisight.media.core.model.entity.MediaAsset;
import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.repository.MediaAssetRepository;
import com.furnisight.media.infrastructure.repository.jpa.MediaAssetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MediaAssetRepositoryImpl implements MediaAssetRepository {
    private final MediaAssetJpaRepository jpaRepository;

    @Override
    public MediaAsset findById(UUID id) {
        return jpaRepository.findById(id).orElseThrow(() -> new MediaNotFoundException(id.toString()));
    }

    @Override
    public MediaAsset save(MediaAsset mediaAsset) {
        return jpaRepository.save(mediaAsset);
    }

    @Override
    public List<MediaAsset> findByStateAndCreatedAtBefore(AssetState state, LocalDateTime time) {
        return jpaRepository.findByStateAndCreatedAtBefore(state, time);
    }

    @Override
    public void delete(MediaAsset mediaAsset) {
        jpaRepository.delete(mediaAsset);
    }
}
