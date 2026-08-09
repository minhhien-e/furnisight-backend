package com.furnisight.catalog.infrastructure.integration.grpc;

import com.furnisight.media.DeleteMediaRequest;
import com.furnisight.media.DeleteMediaResponse;
import com.furnisight.media.GetMediaUrlRequest;
import com.furnisight.media.GetMediaUrlResponse;
import com.furnisight.media.MediaServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GrpcMediaClient {

    private static final long DEFAULT_URL_TTL_SECONDS = 3600;

    @GrpcClient("media-service")
    private MediaServiceGrpc.MediaServiceBlockingStub mediaServiceBlockingStub;

    public GetMediaUrlResponse getMediaUrl(UUID mediaId) {
        GetMediaUrlRequest request = GetMediaUrlRequest.newBuilder()
            .setMediaId(mediaId.toString())
            .setTtlSeconds(DEFAULT_URL_TTL_SECONDS)
            .build();
        return mediaServiceBlockingStub.getMediaUrl(request);
    }

    public DeleteMediaResponse deleteMedia(UUID mediaId) {
        return mediaServiceBlockingStub.deleteMedia(DeleteMediaRequest.newBuilder()
                .setMediaId(mediaId.toString())
                .build());
    }
}
