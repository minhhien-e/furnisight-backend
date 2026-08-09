package com.furnisight.user.domain.events.favorite;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductUnfavoritedEvent(
    UUID id,
    UUID accountId,
    UUID productId,
    LocalDateTime occurredOn
) implements DomainEvent {
}
