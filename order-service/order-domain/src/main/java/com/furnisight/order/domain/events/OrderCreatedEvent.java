package com.furnisight.order.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrderCreatedEvent implements DomainEvent {
    @Builder.Default
    private final UUID eventId = UUID.randomUUID();
    @Builder.Default
    private final LocalDateTime occurredOn = LocalDateTime.now();
    
    private final String orderCode;
    private final String customerEmail;

    @Override
    public UUID eventId() { return eventId; }

    @Override
    public LocalDateTime occurredOn() { return occurredOn; }

    @Override
    public String type() { return "OrderCreated"; }
}
