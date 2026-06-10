package com.furnisight.media.service;

import com.cloudinary.Cloudinary;
import com.furnisight.media.dto.request.InitUploadRequest;
import com.furnisight.media.entity.MediaAsset;
import com.furnisight.media.enums.MediaType;
import com.furnisight.media.enums.OwnerType;
import com.furnisight.media.repository.MediaAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediaServiceTest {

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        Cloudinary cloudinary = new Cloudinary(Map.of(
                "cloud_name", "test",
                "api_key", "key",
                "api_secret", "secret"));
        MediaAssetRepository repository = mock(MediaAssetRepository.class);
        when(repository.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mediaService = new MediaService(cloudinary, repository);
    }

    @Test
    void acceptsGlbAtExactlyOneHundredMegabytes() {
        assertThatCode(() -> mediaService.initUpload(modelRequest(
                "chair.glb", "model/gltf-binary", 100L * 1024 * 1024)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsGlbOverOneHundredMegabytes() {
        assertThatThrownBy(() -> mediaService.initUpload(modelRequest(
                "chair.glb", "model/gltf-binary", 100L * 1024 * 1024 + 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100 MB");
    }

    @Test
    void rejectsInvalidProductModelExtensionOrMimeType() {
        assertThatThrownBy(() -> mediaService.initUpload(modelRequest(
                "chair.gltf", "model/gltf-binary", 1024)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(".glb");

        assertThatThrownBy(() -> mediaService.initUpload(modelRequest(
                "chair.glb", "model/gltf+json", 1024)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contentType");
    }

    @Test
    void acceptsCloudinaryRawPublicIdWithInitializedFileExtension() {
        MediaAsset asset = MediaAsset.builder()
                .cloudinaryPublicId("owner/session-chair")
                .originalFilename("chair.glb")
                .mediaType(MediaType.DOCUMENT)
                .build();

        assertThat(mediaService.matchesInitializedPublicId(asset, "owner/session-chair.glb")).isTrue();
    }

    @Test
    void rejectsDifferentRawPublicIdAndImagePublicIdWithAddedExtension() {
        MediaAsset modelAsset = MediaAsset.builder()
                .cloudinaryPublicId("owner/session-chair")
                .originalFilename("chair.glb")
                .mediaType(MediaType.DOCUMENT)
                .build();
        MediaAsset imageAsset = MediaAsset.builder()
                .cloudinaryPublicId("owner/session-image")
                .originalFilename("image.png")
                .mediaType(MediaType.IMAGE)
                .build();

        assertThat(mediaService.matchesInitializedPublicId(modelAsset, "owner/another-chair.glb")).isFalse();
        assertThat(mediaService.matchesInitializedPublicId(imageAsset, "owner/session-image.png")).isFalse();
    }

    private InitUploadRequest modelRequest(String filename, String contentType, long sizeBytes) {
        InitUploadRequest request = new InitUploadRequest();
        request.setFileName(filename);
        request.setContentType(contentType);
        request.setSizeBytes(sizeBytes);
        request.setOwnerType(OwnerType.PRODUCT_MODEL);
        request.setOwnerId(UUID.randomUUID());
        return request;
    }
}
