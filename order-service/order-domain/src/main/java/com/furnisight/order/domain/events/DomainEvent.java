package com.furnisight.order.domain.events;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent extends Serializable {
    UUID eventId();

    LocalDateTime occurredOn();

    String type();
}
