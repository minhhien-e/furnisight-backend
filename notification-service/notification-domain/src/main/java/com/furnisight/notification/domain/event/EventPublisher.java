package com.furnisight.notification.domain.event;

public interface EventPublisher {
    void publish(DomainEvent event);
}
