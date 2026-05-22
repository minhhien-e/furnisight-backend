package com.furnisight.user.domain.events.favorite;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductFavoritedEvent(
    UUID favoriteId,
    UUID accountId,
    UUID productId,
    LocalDateTime occurredAt
) implements DomainEvent {
}
