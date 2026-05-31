package com.furnisight.user.infrastructure.integration.remote;

import com.furnisight.user.application.profile.port.out.ProfileMediaUrlResolver;
import com.furnisight.user.infrastructure.integration.grpc.GrpcMediaClient;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteProfileMediaUrlResolver implements ProfileMediaUrlResolver {

    private final GrpcMediaClient grpcMediaClient;

    @Override
    public Optional<String> resolveUrl(UUID mediaId) {
        try {
            String url = grpcMediaClient.getMediaUrl(mediaId).getUrl();
            return url == null || url.isBlank() ? Optional.empty() : Optional.of(url);
        } catch (StatusRuntimeException ex) {
            log.warn("Could not resolve profile media url for mediaId={}: {}", mediaId, ex.getStatus());
            return Optional.empty();
        }
    }
}
