package com.furnisight.order.domain.events;

public interface EventPublisher {
    void publish(DomainEvent event);
}
