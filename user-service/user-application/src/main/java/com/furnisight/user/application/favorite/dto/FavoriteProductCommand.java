package com.furnisight.user.application.favorite.dto;

import java.util.UUID;

public record FavoriteProductCommand(
    UUID accountId,
    UUID productId
) {
}
