package com.furnisight.catalog.infrastructure.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductUnfavoritedEvent(
    UUID id,
    UUID accountId,
    UUID productId,
    LocalDateTime occurredOn
) {
}
