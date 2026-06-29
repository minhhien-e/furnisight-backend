package com.furnisight.user.application.favorite.dto;

import java.util.UUID;

public record GetFavoriteProductsQuery(
        UUID accountId,
        String locale
) {
}
