package com.furnisight.media.grpc;

import com.furnisight.media.GetMediaUrlRequest;
import com.furnisight.media.GetMediaUrlResponse;
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
            .build());
        responseObserver.onCompleted();
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
