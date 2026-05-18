package com.furnisight.media.infrastructure.repository.jpa;

import com.furnisight.media.core.model.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import com.furnisight.media.core.model.enums.AssetState;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MediaAssetJpaRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findByStateAndCreatedAtBefore(AssetState state, LocalDateTime time);
}
