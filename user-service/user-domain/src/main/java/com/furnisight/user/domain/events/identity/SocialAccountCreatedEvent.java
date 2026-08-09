package com.furnisight.user.domain.events.identity;

import com.furnisight.user.domain.seedwork.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record SocialAccountCreatedEvent(
    UUID accountId,
    String email,
    String password,
    LocalDateTime occurredAt
) implements DomainEvent {
}
