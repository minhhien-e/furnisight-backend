package com.furnisight.user.presentation.web.rest.dto.response;

import com.furnisight.user.application.favorite.dto.CatalogFavoriteProductSummary;
import com.furnisight.user.application.favorite.dto.FavoriteProductProjection;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteProductResponse(
    UUID id,
    UUID accountId,
    UUID productId,
    LocalDateTime createdAt,
    ProductSummaryResponse product
) {
    public static FavoriteProductResponse from(FavoriteProductProjection favoriteProduct) {
        return new FavoriteProductResponse(
            favoriteProduct.id(),
            favoriteProduct.accountId(),
            favoriteProduct.productId(),
            favoriteProduct.createdAt(),
            ProductSummaryResponse.from(favoriteProduct.product())
        );
    }

    public record ProductSummaryResponse(
        UUID id,
        String slug,
        String name,
        String image,
        String categoryName,
        Double price,
        Double oldPrice,
        Integer soldCount
    ) {
        public static ProductSummaryResponse from(CatalogFavoriteProductSummary product) {
            if (product == null) {
                return null;
            }

            return new ProductSummaryResponse(
                product.id(),
                product.slug(),
                product.name(),
                product.image(),
                product.categoryName(),
                product.price(),
                product.oldPrice(),
                product.soldCount()
            );
        }
    }
}
