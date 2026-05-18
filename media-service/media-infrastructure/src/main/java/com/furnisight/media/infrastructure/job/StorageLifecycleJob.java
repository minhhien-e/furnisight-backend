package com.furnisight.media.infrastructure.job;

import com.furnisight.media.core.model.entity.MediaAsset;
import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.repository.MediaAssetRepository;
import com.furnisight.media.core.storage.StorageProvider;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageLifecycleJob {

    private final MediaAssetRepository mediaAssetRepository;
    private final StorageProvider storageProvider;
    private final MeterRegistry meterRegistry;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupStalePendingUploads() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<MediaAsset> staleAssets = mediaAssetRepository.findByStateAndCreatedAtBefore(AssetState.PENDING_UPLOAD, threshold);

        int count = 0;
        for (MediaAsset asset : staleAssets) {
            try {
                if (storageProvider.exists(asset.getOriginalKey())) {
                    storageProvider.delete(asset.getOriginalKey());
                }
                mediaAssetRepository.delete(asset);
                count++;
                meterRegistry.counter("media.presigned.unused", "status", "timeout").increment();
            } catch (Exception e) {
                log.error("Failed to delete stale asset: {}", asset.getId(), e);
            }
        }
        log.info("Finished cleanup. Deleted {} stale PENDING_UPLOAD assets.", count);
    }

    // Run every day at 3 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupDeletedAssets() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<MediaAsset> deletedAssets = mediaAssetRepository.findByStateAndCreatedAtBefore(AssetState.DELETED, threshold);

        int count = 0;
        for (MediaAsset asset : deletedAssets) {
            try {
                if (storageProvider.exists(asset.getOriginalKey())) {
                    storageProvider.delete(asset.getOriginalKey());
                }
                mediaAssetRepository.delete(asset);
                count++;
            } catch (Exception e) {
                log.error("Failed to dynamically/physically delete asset: {}", asset.getId(), e);
            }
        }
        log.info("Finished cleanup. Deleted {} logically DELETED assets > 30 days.", count);
    }
}
