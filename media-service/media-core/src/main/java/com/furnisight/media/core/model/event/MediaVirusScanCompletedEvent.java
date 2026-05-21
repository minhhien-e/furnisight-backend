package com.furnisight.media.core.model.event;

import java.time.Instant;
import java.util.UUID;

public record MediaVirusScanCompletedEvent(
    UUID mediaId,
    boolean infected,
    String virusName,
    Instant occurredAt
) implements DomainEvent, InternalEvent {
}
