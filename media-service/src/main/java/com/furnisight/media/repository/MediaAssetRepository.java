package com.furnisight.media.repository;

import com.furnisight.media.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
}
