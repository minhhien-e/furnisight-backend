package com.furnisight.media.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.media.api.dto.request.InitUploadRequest;
import com.furnisight.media.api.dto.response.InitUploadResponse;
import com.furnisight.media.api.dto.response.MediaStatusResponse;
import com.furnisight.media.api.service.IdempotencyService;
import com.furnisight.media.api.service.MediaService;
import com.furnisight.media.core.detector.MediaTypeDetector;
import com.furnisight.media.core.model.entity.MediaAsset;
import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.model.enums.HttpMethod;
import com.furnisight.media.core.model.enums.MediaType;
import com.furnisight.media.core.model.event.MediaUploadedEvent;
import com.furnisight.media.core.publisher.InternalEventPublisher;
import com.furnisight.media.core.repository.MediaAssetRepository;
import com.furnisight.media.core.storage.StorageProvider;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {
    private final MediaAssetRepository mediaAssetRepository;
    private final StorageProvider storageProvider;
    private final InternalEventPublisher eventPublisher;
    private final IdempotencyService idempotencyService;
    private final MediaTypeDetector mediaTypeDetector;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public InitUploadResponse initUpload(InitUploadRequest request) {
        String key = "users/" + request.getOwnerId() + "/" + UUID.randomUUID() + "_" + request.getFileName();

        MediaAsset asset = new MediaAsset();
        asset.setOriginalFilename(request.getFileName());

        MediaType detectedMediaType = mediaTypeDetector.detect(request.getContentType(), request.getFileName());
        asset.setMediaType(detectedMediaType);

        asset.setMimeType(request.getContentType());
        asset.setSizeBytes(request.getSizeBytes());
        asset.setOwnerType(request.getOwnerType());
        asset.setOwnerId(request.getOwnerId());
        asset.setOriginalKey(key);
        asset.setState(AssetState.PENDING_UPLOAD);
        mediaAssetRepository.save(asset);

        String uploadUrl = storageProvider.generatePresignedUrl(key, HttpMethod.PUT, Duration.ofMinutes(15));

        var response = InitUploadResponse.builder()
            .mediaId(asset.getId())
            .uploadUrl(uploadUrl)
            .state(asset.getState().name())
            .build();
        try {
            idempotencyService.save(request.getIdempotencyKey(), objectMapper.writeValueAsString(response), Duration.ofMinutes(15).getSeconds());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Override
    @Transactional
    public void completeUpload(UUID mediaId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaId);

        if (asset.getState() != AssetState.PENDING_UPLOAD) {
            throw new IllegalStateException("Asset is not in PENDING_UPLOAD state");
        }

        asset.setState(AssetState.UPLOADED);
        mediaAssetRepository.save(asset);

        eventPublisher.publish(MediaUploadedEvent.builder()
            .mediaId(mediaId)
            .occurredAt(Instant.now())
            .build());
    }

    @Override
    @Transactional(readOnly = true)
    public MediaStatusResponse getMediaById(UUID mediaId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaId);

        String downloadUrl = null;
        if (asset.getState() == AssetState.ACTIVE) {
            downloadUrl = storageProvider.generatePresignedUrl(asset.getOriginalKey(), HttpMethod.GET, Duration.ofHours(1));

            meterRegistry.counter("media.private.url.generated", "type", asset.getMediaType().name()).increment();
            log.info("[AUDIT] Caller requested Private Signed URL for Asset '{}' (Owner '{}')",
                     mediaId, asset.getOwnerId());
        }

        return MediaStatusResponse.builder()
            .mediaId(asset.getId())
            .originalFilename(asset.getOriginalFilename())
            .state(asset.getState())
            .downloadUrl(downloadUrl)
            .rejectReason(asset.getRejectReason())
            .variants(new HashMap<>())
            .build();
    }
}
