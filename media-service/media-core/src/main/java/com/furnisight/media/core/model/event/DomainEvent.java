package com.furnisight.media.core.model.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
