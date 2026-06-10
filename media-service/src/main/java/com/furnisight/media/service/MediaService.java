package com.furnisight.media.service;

import com.cloudinary.Cloudinary;
import com.furnisight.media.dto.request.CompleteUploadRequest;
import com.furnisight.media.dto.request.InitUploadRequest;
import com.furnisight.media.dto.response.InitUploadResponse;
import com.furnisight.media.dto.response.MediaResponse;
import com.furnisight.media.entity.MediaAsset;
import com.furnisight.media.enums.AssetState;
import com.furnisight.media.enums.MediaType;
import com.furnisight.media.exception.MediaNotFoundException;
import com.furnisight.media.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private static final long PRODUCT_MODEL_MAX_SIZE_BYTES = 100L * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional
    public InitUploadResponse initUpload(InitUploadRequest request) {
        validateInitRequest(request);

        MediaType mediaType = resolveMediaType(request.getContentType(), request.getFileName());
        String publicId = buildPublicId(request);

        MediaAsset asset = MediaAsset.builder()
                .cloudinaryPublicId(publicId)
                .ownerId(request.getOwnerId())
                .ownerType(request.getOwnerType())
                .mediaType(mediaType)
                .state(AssetState.UPLOADING)
                .originalFilename(request.getFileName())
                .mimeType(request.getContentType())
                .sizeBytes(request.getSizeBytes())
                .build();

        mediaAssetRepository.save(asset);

        long timestamp = Instant.now().getEpochSecond();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("api_key", cloudinary.config.apiKey);
        fields.put("timestamp", timestamp);
        fields.put("public_id", publicId);

        if (request.getFolder() != null && !request.getFolder().isBlank()) {
            fields.put("folder", request.getFolder());
        }

        Map<String, Object> paramsToSign = new LinkedHashMap<>();
        paramsToSign.put("public_id", publicId);
        paramsToSign.put("timestamp", timestamp);
        if (request.getFolder() != null && !request.getFolder().isBlank()) {
            paramsToSign.put("folder", request.getFolder());
        }
        fields.put("signature", cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret));

        String uploadUrl = String.format(
                "https://api.cloudinary.com/v1_1/%s/%s/upload",
                cloudinary.config.cloudName,
                toResourceType(mediaType));

        return new InitUploadResponse(asset.getId(), uploadUrl, asset.getState().name(), fields);
    }

    @Transactional
    public MediaResponse completeUpload(UUID mediaId, CompleteUploadRequest request) {
        MediaAsset asset = mediaAssetRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException(mediaId));

        if (asset.getState() != AssetState.UPLOADING) {
            throw new IllegalArgumentException("Media asset is not waiting for upload completion");
        }

        String publicId = request.getPublicId() != null ? request.getPublicId() : asset.getCloudinaryPublicId();
        if (publicId == null || !publicId.equals(asset.getCloudinaryPublicId())) {
            throw new IllegalArgumentException("Uploaded public_id does not match the initialized media session");
        }
        String completedUrl = request.getSecureUrl() != null ? request.getSecureUrl() : request.getUrl();
        if (completedUrl == null || completedUrl.isBlank()) {
            throw new IllegalArgumentException("Uploaded media URL is required");
        }
        String expectedResourceType = toResourceType(asset.getMediaType());
        if (request.getResourceType() != null
                && !request.getResourceType().isBlank()
                && !expectedResourceType.equalsIgnoreCase(request.getResourceType())) {
            throw new IllegalArgumentException("Uploaded resource_type does not match the initialized media session");
        }
        Map<?, ?> uploadedResource;
        try {
            uploadedResource = cloudinary.api().resource(publicId,
                    Map.of("resource_type", expectedResourceType));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Uploaded asset could not be verified with Cloudinary", ex);
        }
        Object secureUrl = uploadedResource.get("secure_url");
        Object plainUrl = uploadedResource.get("url");
        String verifiedUrl = String.valueOf(secureUrl != null ? secureUrl : plainUrl);
        if (!completedUrl.equals(verifiedUrl)) {
            throw new IllegalArgumentException("Uploaded media URL does not match the initialized media session");
        }
        Object verifiedBytes = uploadedResource.get("bytes");
        if (verifiedBytes instanceof Number bytes && bytes.longValue() > asset.getSizeBytes()) {
            throw new IllegalArgumentException("Uploaded media size exceeds the initialized media session");
        }

        asset.setCloudinaryPublicId(publicId);
        asset.setUrl(String.valueOf(plainUrl != null ? plainUrl : completedUrl));
        asset.setSecureUrl(verifiedUrl);
        asset.setFormat(request.getFormat());
        asset.setWidth(request.getWidth());
        asset.setHeight(request.getHeight());
        if (verifiedBytes instanceof Number bytes) {
            asset.setSizeBytes(bytes.longValue());
        }
        if (request.getOriginalFilename() != null && !request.getOriginalFilename().isBlank()) {
            asset.setOriginalFilename(request.getOriginalFilename());
        }
        asset.setState(AssetState.ACTIVE);

        mediaAssetRepository.save(asset);
        return MediaResponse.from(asset);
    }

    @Transactional
    public void cancelUpload(UUID mediaId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException(mediaId));

        deleteFromCloudinary(asset);
        mediaAssetRepository.delete(asset);
        log.info("Cancelled media upload id={} publicId={}", mediaId, asset.getCloudinaryPublicId());
    }

    /**
     * Lấy thông tin asset theo ID.
     */
    @Transactional(readOnly = true)
    public MediaResponse getById(UUID id) {
        MediaAsset asset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException(id));
        return MediaResponse.from(asset);
    }

    /**
     * Xóa asset khỏi Cloudinary và DB.
     */
    @Transactional
    public void delete(UUID id) {
        MediaAsset asset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException(id));
        deleteFromCloudinary(asset);
        mediaAssetRepository.delete(asset);
        log.info("Deleted media id={} publicId={}", id, asset.getCloudinaryPublicId());
    }

    private void deleteFromCloudinary(MediaAsset asset) {
        if (asset.getCloudinaryPublicId() == null || asset.getCloudinaryPublicId().isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(asset.getCloudinaryPublicId(),
                    Map.of("resource_type", toResourceType(asset.getMediaType())));
        } catch (IOException e) {
            log.error("Cloudinary delete failed for publicId={}", asset.getCloudinaryPublicId(), e);
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private MediaType resolveMediaType(String mimeType, String filename) {
        if (mimeType == null)
            return MediaType.DOCUMENT;
        String m = mimeType.toLowerCase();
        if (m.startsWith("image/"))
            return MediaType.IMAGE;
        if (m.startsWith("video/"))
            return MediaType.VIDEO;
        if (m.startsWith("audio/"))
            return MediaType.AUDIO;
        return MediaType.DOCUMENT;
    }

    private String toResourceType(MediaType type) {
        return switch (type) {
            case VIDEO -> "video";
            case AUDIO -> "video"; // Cloudinary uses "video" for audio too
            case IMAGE -> "image";
            default -> "raw";
        };
    }

    private void validateInitRequest(InitUploadRequest request) {
        if (request.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        if (request.getOwnerType() == null) {
            throw new IllegalArgumentException("ownerType is required");
        }
        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new IllegalArgumentException("fileName is required");
        }
        if (request.getContentType() == null || request.getContentType().isBlank()) {
            throw new IllegalArgumentException("contentType is required");
        }
        if (request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
            throw new IllegalArgumentException("sizeBytes must be greater than 0");
        }
        if (request.getOwnerType() == com.furnisight.media.enums.OwnerType.PRODUCT_MODEL) {
            String filename = request.getFileName().toLowerCase();
            String contentType = request.getContentType().toLowerCase();
            if (!filename.endsWith(".glb")) {
                throw new IllegalArgumentException("Product model must use the .glb extension");
            }
            if (!contentType.equals("model/gltf-binary")
                    && !contentType.equals("application/octet-stream")) {
                throw new IllegalArgumentException("Product model contentType must be model/gltf-binary or application/octet-stream");
            }
            if (request.getSizeBytes() > PRODUCT_MODEL_MAX_SIZE_BYTES) {
                throw new IllegalArgumentException("Product model must not exceed 100 MB");
            }
        }
    }

    private String buildPublicId(InitUploadRequest request) {
        String baseName = request.getFileName().replaceAll("\\.[^.]+$", "");
        String safeName = baseName.toLowerCase().replaceAll("[^a-z0-9_-]+", "-").replaceAll("(^-|-$)", "");
        if (safeName.isBlank()) {
            safeName = "media";
        }

        return request.getOwnerId() + "/" + UUID.randomUUID() + "-" + safeName;
    }

}
