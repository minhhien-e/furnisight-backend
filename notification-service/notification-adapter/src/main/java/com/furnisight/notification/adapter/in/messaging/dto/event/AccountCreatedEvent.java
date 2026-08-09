package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountCreatedEvent(
    UUID accountId,
    String email,
    LocalDateTime occurredAt
) {
}
