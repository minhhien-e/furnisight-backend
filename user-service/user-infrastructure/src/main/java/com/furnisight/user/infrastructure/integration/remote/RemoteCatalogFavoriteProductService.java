package com.furnisight.user.infrastructure.integration.remote;

import com.furnisight.catalog.FavoriteProductSummary;
import com.furnisight.user.application.favorite.dto.CatalogFavoriteProductSummary;
import com.furnisight.user.application.favorite.port.out.CatalogFavoriteProductService;
import com.furnisight.user.infrastructure.integration.grpc.GrpcCatalogClient;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemoteCatalogFavoriteProductService implements CatalogFavoriteProductService {
    private final GrpcCatalogClient grpcCatalogClient;

    @Override
    public Map<UUID, CatalogFavoriteProductSummary> getFavoriteProductSummaries(Collection<UUID> productIds, String locale) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        try {
            Map<UUID, CatalogFavoriteProductSummary> products = new LinkedHashMap<>();
            for (FavoriteProductSummary product : grpcCatalogClient
                    .getFavoriteProductSummaries(productIds, locale)
                    .getProductsList()) {
                UUID productId = UUID.fromString(product.getId());
                products.put(productId, toSummary(productId, product));
            }
            return products;
        } catch (StatusRuntimeException e) {
            log.warn("Catalog favorite summaries are temporarily unavailable: {}", e.getStatus());
            return Map.of();
        }
    }

    private CatalogFavoriteProductSummary toSummary(UUID productId, FavoriteProductSummary product) {
        return new CatalogFavoriteProductSummary(
            productId,
            product.getSlug(),
            product.getName(),
            product.getImage(),
            product.getCategoryName(),
            product.hasPrice() ? product.getPrice() : null,
            product.getSoldCount()
        );
    }
}
