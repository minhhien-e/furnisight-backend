package com.furnisight.order.domain.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPaidEvent implements DomainEvent {
    @Builder.Default
    private final java.util.UUID eventId = java.util.UUID.randomUUID();
    @Builder.Default
    private final java.time.LocalDateTime occurredOn = java.time.LocalDateTime.now();

    private final String orderCode;
    private final Double paidAmount;
    private final String paymentMethod;

    @Override
    public java.util.UUID eventId() { return eventId; }

    @Override
    public java.time.LocalDateTime occurredOn() { return occurredOn; }

    @Override
    public String type() { return "OrderPaid"; }
}
