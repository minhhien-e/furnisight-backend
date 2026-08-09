package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountVerificationRequestedEvent (
    UUID accountId,
    String destination,
    String verifyUrl,
    LocalDateTime occurredAt
) {
}
