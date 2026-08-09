package com.furnisight.user.application.favorite.dto;

import java.util.UUID;

public record CatalogFavoriteProductSummary(
    UUID id,
    String slug,
    String name,
    String image,
    String categoryName,
    Double price,
    Integer soldCount
) {
}
