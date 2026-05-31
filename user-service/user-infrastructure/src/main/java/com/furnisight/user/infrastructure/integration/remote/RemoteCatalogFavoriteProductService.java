package com.furnisight.user.infrastructure.integration.remote;

import com.furnisight.catalog.FavoriteProductSummary;
import com.furnisight.user.application.favorite.dto.CatalogFavoriteProductSummary;
import com.furnisight.user.application.favorite.port.out.CatalogFavoriteProductService;
import com.furnisight.user.infrastructure.integration.grpc.GrpcCatalogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoteCatalogFavoriteProductService implements CatalogFavoriteProductService {
    private final GrpcCatalogClient grpcCatalogClient;

    @Override
    public Map<UUID, CatalogFavoriteProductSummary> getFavoriteProductSummaries(Collection<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, CatalogFavoriteProductSummary> products = new LinkedHashMap<>();
        for (FavoriteProductSummary product : grpcCatalogClient.getFavoriteProductSummaries(productIds).getProductsList()) {
            UUID productId = UUID.fromString(product.getId());
            products.put(productId, toSummary(productId, product));
        }
        return products;
    }

    private CatalogFavoriteProductSummary toSummary(UUID productId, FavoriteProductSummary product) {
        return new CatalogFavoriteProductSummary(
            productId,
            product.getSlug(),
            product.getName(),
            product.getImage(),
            product.getCategoryName(),
            product.hasPrice() ? product.getPrice() : null,
            product.hasOldPrice() ? product.getOldPrice() : null,
            product.getSoldCount()
        );
    }
}
