package com.furnisight.media.grpc;

import com.furnisight.media.GetMediaUrlRequest;
import com.furnisight.media.GetMediaUrlResponse;
import com.furnisight.media.DeleteMediaRequest;
import com.furnisight.media.DeleteMediaResponse;
import com.furnisight.media.MediaServiceGrpc;
import com.furnisight.media.entity.MediaAsset;
import com.furnisight.media.enums.AssetState;
import com.furnisight.media.repository.MediaAssetRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcMediaService extends MediaServiceGrpc.MediaServiceImplBase {

    private final MediaAssetRepository mediaAssetRepository;
    private final com.furnisight.media.service.MediaService mediaService;

    @Override
    public void getMediaUrl(GetMediaUrlRequest request,
                            StreamObserver<GetMediaUrlResponse> responseObserver) {
        UUID mediaId = parseId(request.getMediaId());

        MediaAsset asset = mediaAssetRepository.findById(mediaId).orElse(null);
        if (asset == null) {
            responseObserver.onError(
                Status.NOT_FOUND.withDescription("Media not found: " + mediaId).asRuntimeException());
            return;
        }

        String url = asset.getState() == AssetState.ACTIVE ? asset.getSecureUrl() : "";

        responseObserver.onNext(GetMediaUrlResponse.newBuilder()
            .setMediaId(asset.getId().toString())
            .setUrl(url)
            .setState(asset.getState().name())
            .setMimeType(asset.getMimeType() == null ? "" : asset.getMimeType())
            .setOriginalFilename(asset.getOriginalFilename() == null ? "" : asset.getOriginalFilename())
            .setOwnerType(asset.getOwnerType().name())
            .setMediaType(asset.getMediaType().name())
            .setSizeBytes(asset.getSizeBytes() == null ? 0L : asset.getSizeBytes())
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMedia(DeleteMediaRequest request,
                            StreamObserver<DeleteMediaResponse> responseObserver) {
        UUID mediaId = parseId(request.getMediaId());
        try {
            mediaService.delete(mediaId);
            responseObserver.onNext(DeleteMediaResponse.newBuilder().setDeleted(true).build());
            responseObserver.onCompleted();
        } catch (com.furnisight.media.domain.exceptions.NotFoundException ex) {
            responseObserver.onError(
                Status.NOT_FOUND.withDescription("Media not found: " + mediaId).asRuntimeException());
        }
    }

    private UUID parseId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw Status.INVALID_ARGUMENT
                .withDescription("media_id must be a valid UUID")
                .withCause(ex)
                .asRuntimeException();
        }
    }
}
