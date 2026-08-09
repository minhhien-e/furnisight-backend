package com.furnisight.review.adapter.out.client;

import com.furnisight.review.adapter.out.client.GrpcMediaClient;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteMediaUrlResolver {

    private final GrpcMediaClient grpcMediaClient;

    public Optional<String> resolveUrl(UUID mediaId) {
        if (mediaId == null) {
            return Optional.empty();
        }
        try {
            String url = grpcMediaClient.getMediaUrl(mediaId).getUrl();
            return url == null || url.isBlank() ? Optional.empty() : Optional.of(url);
        } catch (StatusRuntimeException ex) {
            log.warn("Could not resolve media url for mediaId={}: {}", mediaId, ex.getStatus());
            return Optional.empty();
        }
    }
}

