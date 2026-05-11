package com.furnisight.user.domain.events.profile;

import java.time.LocalDateTime;
import java.util.UUID;

import com.furnisight.user.domain.seedwork.DomainEvent;

public record ProfileUpdatedEvent(UUID accountId, UUID profileId, LocalDateTime occurredAt) implements DomainEvent{}
