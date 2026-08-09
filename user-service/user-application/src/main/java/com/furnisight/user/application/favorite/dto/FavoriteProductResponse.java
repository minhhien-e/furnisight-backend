package com.furnisight.user.application.favorite.dto;

import com.furnisight.user.domain.entities.favorite.FavoriteProduct;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteProductResponse(
    UUID id,
    UUID accountId,
    UUID productId,
    LocalDateTime createdAt,
    CatalogFavoriteProductSummary product
) {
    public static FavoriteProductResponse from(
        FavoriteProduct favoriteProduct,
        CatalogFavoriteProductSummary product
    ) {
        return new FavoriteProductResponse(
            favoriteProduct.getId(),
            favoriteProduct.getAccountId(),
            favoriteProduct.getProductId(),
            favoriteProduct.getCreatedAt(),
            product
        );
    }
}
