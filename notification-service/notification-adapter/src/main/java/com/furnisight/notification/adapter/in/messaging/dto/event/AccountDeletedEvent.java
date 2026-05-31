package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountDeletedEvent(
    UUID accountId,
    String email,
    LocalDateTime occurredAt
) {
}
