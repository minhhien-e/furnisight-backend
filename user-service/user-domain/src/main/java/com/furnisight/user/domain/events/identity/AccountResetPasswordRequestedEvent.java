package com.furnisight.user.domain.events.identity;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResetPasswordRequestedEvent(
    UUID  accountId,
    String token,
    String destination,
    LocalDateTime occurredAt
) implements DomainEvent {
}
