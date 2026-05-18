package com.furnisight.media.core.model.event;

import java.time.Instant;
import java.util.UUID;
public record MediaValidatedEvent(
    UUID mediaId,
    String detectedMimeType,
    boolean valid,
    Instant occurredAt
) implements DomainEvent, InternalEvent{}

