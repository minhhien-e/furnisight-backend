package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResetPasswordRequestedEvent(
    UUID accountId,
    String token,
    String destination,
    LocalDateTime occurredAt
) {
}
