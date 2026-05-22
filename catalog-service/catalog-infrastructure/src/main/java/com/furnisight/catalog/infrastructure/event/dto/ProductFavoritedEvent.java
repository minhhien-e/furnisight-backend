package com.furnisight.catalog.infrastructure.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductFavoritedEvent(
    UUID favoriteId,
    UUID accountId,
    UUID productId,
    LocalDateTime occurredAt
) {
}
