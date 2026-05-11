package com.furnisight.notification.application.outbox.port.in.usecase;

import com.furnisight.notification.domain.event.DomainEvent;

import java.util.List;

public interface AddOutboxEventUseCase {
    void addDomainEvents(List<DomainEvent> events);
}
