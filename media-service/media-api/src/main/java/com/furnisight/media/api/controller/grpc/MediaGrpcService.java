package com.furnisight.media.api.controller.grpc;

import com.furnisight.media.GetMediaRequest;
import com.furnisight.media.MediaResponse;
import com.furnisight.media.MediaServiceGrpc;
import com.furnisight.media.MediaStatus;
import com.furnisight.media.api.service.MediaService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class MediaGrpcService extends MediaServiceGrpc.MediaServiceImplBase {
    private final MediaService mediaService;

    @Override
    public void getMedia(GetMediaRequest request, StreamObserver<MediaResponse> responseObserver) {
        var result = mediaService.getMediaById(UUID.fromString(request.getId()));
        var response = MediaResponse.newBuilder()
            .setId(request.getId())
            .setDownloadUrl(result.getDownloadUrl())
            .setName(result.getOriginalFilename())
            .setStatus(MediaStatus.valueOf((result.getState().name())))
            .setContentType("")
            .setPreviewUrl("")
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
