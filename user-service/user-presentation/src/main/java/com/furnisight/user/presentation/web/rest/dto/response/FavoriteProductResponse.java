package com.furnisight.user.presentation.web.rest.dto.response;

import com.furnisight.user.application.favorite.dto.CatalogFavoriteProductSummary;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteProductResponse(
    UUID id,
    UUID accountId,
    UUID productId,
    LocalDateTime createdAt,
    ProductResponse product
) {
    public static FavoriteProductResponse from(
        com.furnisight.user.application.favorite.dto.FavoriteProductResponse favoriteProduct
    ) {
        return new FavoriteProductResponse(
            favoriteProduct.id(),
            favoriteProduct.accountId(),
            favoriteProduct.productId(),
            favoriteProduct.createdAt(),
            ProductResponse.from(favoriteProduct.product())
        );
    }

    public record ProductResponse(
        UUID id,
        String slug,
        String name,
        String image,
        String categoryName,
        Double price,
        Integer soldCount
    ) {
        public static ProductResponse from(CatalogFavoriteProductSummary product) {
            if (product == null) {
                return null;
            }

            return new ProductResponse(
                product.id(),
                product.slug(),
                product.name(),
                product.image(),
                product.categoryName(),
                product.price(),
                product.soldCount()
            );
        }
    }
}
