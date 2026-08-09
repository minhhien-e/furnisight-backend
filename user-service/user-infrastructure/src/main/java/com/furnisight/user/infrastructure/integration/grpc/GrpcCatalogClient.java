package com.furnisight.user.infrastructure.integration.grpc;

import com.furnisight.catalog.CatalogServiceGrpc;
import com.furnisight.catalog.GetFavoriteProductSummariesRequest;
import com.furnisight.catalog.GetFavoriteProductSummariesResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class GrpcCatalogClient {

    @GrpcClient("catalog-service")
    private CatalogServiceGrpc.CatalogServiceBlockingStub catalogServiceBlockingStub;

    public GetFavoriteProductSummariesResponse getFavoriteProductSummaries(Collection<UUID> productIds, String locale) {
        GetFavoriteProductSummariesRequest request = GetFavoriteProductSummariesRequest.newBuilder()
            .addAllProductIds(productIds.stream()
                .map(UUID::toString)
                .toList())
            .setLocale(normalizeLocale(locale))
            .build();
        return catalogServiceBlockingStub.getFavoriteProductSummaries(request);
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "vi";
        }
        String normalized = locale.trim().toLowerCase();
        return normalized.startsWith("en") ? "en" : "vi";
    }
}
