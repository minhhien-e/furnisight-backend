package com.furnisight.media.service;

import com.cloudinary.Cloudinary;
import com.furnisight.media.dto.request.UploadMediaRequest;
import com.furnisight.media.dto.response.MediaResponse;
import com.furnisight.media.entity.MediaAsset;
import com.furnisight.media.enums.AssetState;
import com.furnisight.media.enums.MediaType;
import com.furnisight.media.exception.MediaNotFoundException;
import com.furnisight.media.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;
    private final Tika tika = new Tika();

    /**
     * Upload file trực tiếp lên Cloudinary và lưu metadata vào DB.
     */
    @Transactional
    public MediaResponse upload(MultipartFile file, UploadMediaRequest request) {
        String detectedMime = detectMimeType(file);
        MediaType mediaType  = resolveMediaType(detectedMime, file.getOriginalFilename());

        Map<String, Object> params = new HashMap<>();
        params.put("resource_type", toResourceType(mediaType));
        if (request.getFolder() != null && !request.getFolder().isBlank()) {
            params.put("folder", request.getFolder());
        }

        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(file.getBytes(), params);
        } catch (IOException e) {
            log.error("Cloudinary upload failed for owner={}", request.getOwnerId(), e);
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }

        MediaAsset asset = MediaAsset.builder()
            .cloudinaryPublicId((String) result.get("public_id"))
            .url((String) result.get("url"))
            .secureUrl((String) result.get("secure_url"))
            .ownerId(request.getOwnerId())
            .ownerType(request.getOwnerType())
            .mediaType(mediaType)
            .state(AssetState.ACTIVE)
            .originalFilename(file.getOriginalFilename())
            .mimeType(detectedMime)
            .sizeBytes(file.getSize())
            .format(getStringOrNull(result, "format"))
            .width(getIntOrNull(result, "width"))
            .height(getIntOrNull(result, "height"))
            .build();

        mediaAssetRepository.save(asset);
        log.info("Uploaded media id={} publicId={} owner={}", asset.getId(), asset.getCloudinaryPublicId(), asset.getOwnerId());
        return MediaResponse.from(asset);
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
        try {
            cloudinary.uploader().destroy(asset.getCloudinaryPublicId(),
                Map.of("resource_type", toResourceType(asset.getMediaType())));
        } catch (IOException e) {
            log.error("Cloudinary delete failed for publicId={}", asset.getCloudinaryPublicId(), e);
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
        mediaAssetRepository.delete(asset);
        log.info("Deleted media id={} publicId={}", id, asset.getCloudinaryPublicId());
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private String detectMimeType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            return file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        }
    }

    private MediaType resolveMediaType(String mimeType, String filename) {
        if (mimeType == null) return MediaType.DOCUMENT;
        String m = mimeType.toLowerCase();
        if (m.startsWith("image/")) return MediaType.IMAGE;
        if (m.startsWith("video/")) return MediaType.VIDEO;
        if (m.startsWith("audio/")) return MediaType.AUDIO;
        return MediaType.DOCUMENT;
    }

    private String toResourceType(MediaType type) {
        return switch (type) {
            case VIDEO -> "video";
            case AUDIO -> "video"; // Cloudinary uses "video" for audio too
            case IMAGE -> "image";
            default    -> "raw";
        };
    }

    private String getStringOrNull(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }

    private Integer getIntOrNull(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v instanceof Number n ? n.intValue() : null;
    }
}
