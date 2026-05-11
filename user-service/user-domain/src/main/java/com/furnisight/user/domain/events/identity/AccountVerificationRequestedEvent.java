package com.furnisight.user.domain.events.identity;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountVerificationRequestedEvent(
    UUID  accountId,
    String verifyUrl,
    LocalDateTime occurredAt
) implements DomainEvent {
}
