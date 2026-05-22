package com.furnisight.user.infrastructure.grpc;

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

    public GetFavoriteProductSummariesResponse getFavoriteProductSummaries(Collection<UUID> productIds) {
        GetFavoriteProductSummariesRequest request = GetFavoriteProductSummariesRequest.newBuilder()
            .addAllProductIds(productIds.stream()
                .map(UUID::toString)
                .toList())
            .build();
        return catalogServiceBlockingStub.getFavoriteProductSummaries(request);
    }
}
