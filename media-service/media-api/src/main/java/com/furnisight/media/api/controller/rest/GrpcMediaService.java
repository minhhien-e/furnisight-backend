package com.furnisight.media.api.controller.rest;

import com.furnisight.media.GetMediaUrlRequest;
import com.furnisight.media.GetMediaUrlResponse;
import com.furnisight.media.MediaServiceGrpc;
import com.furnisight.media.core.model.enums.AssetState;
import com.furnisight.media.core.model.enums.HttpMethod;
import com.furnisight.media.core.repository.MediaAssetRepository;
import com.furnisight.media.core.storage.StorageProvider;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Duration;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcMediaService extends MediaServiceGrpc.MediaServiceImplBase {

    private static final long DEFAULT_TTL_SECONDS = 3600;

    private final MediaAssetRepository mediaAssetRepository;
    private final StorageProvider storageProvider;

    @Override
    public void getMediaUrl(GetMediaUrlRequest request, StreamObserver<GetMediaUrlResponse> responseObserver) {
        UUID mediaId = parseMediaId(request.getMediaId());
        var asset = mediaAssetRepository.findById(mediaId);

        if (asset.getState() != AssetState.ACTIVE) {
            responseObserver.onNext(GetMediaUrlResponse.newBuilder()
                .setMediaId(asset.getId().toString())
                .setState(asset.getState().name())
                .setMimeType(asset.getMimeType() == null ? "" : asset.getMimeType())
                .setOriginalFilename(asset.getOriginalFilename() == null ? "" : asset.getOriginalFilename())
                .build());
            responseObserver.onCompleted();
            return;
        }

        long ttlSeconds = request.getTtlSeconds() > 0 ? request.getTtlSeconds() : DEFAULT_TTL_SECONDS;
        String url = storageProvider.generatePresignedUrl(
            asset.getOriginalKey(),
            HttpMethod.GET,
            Duration.ofSeconds(ttlSeconds)
        );

        responseObserver.onNext(GetMediaUrlResponse.newBuilder()
            .setMediaId(asset.getId().toString())
            .setUrl(url)
            .setState(asset.getState().name())
            .setMimeType(asset.getMimeType() == null ? "" : asset.getMimeType())
            .setOriginalFilename(asset.getOriginalFilename() == null ? "" : asset.getOriginalFilename())
            .build());
        responseObserver.onCompleted();
    }

    private UUID parseMediaId(String rawMediaId) {
        try {
            return UUID.fromString(rawMediaId);
        } catch (IllegalArgumentException ex) {
            throw Status.INVALID_ARGUMENT
                .withDescription("media_id must be a valid UUID")
                .withCause(ex)
                .asRuntimeException();
        }
    }
}
