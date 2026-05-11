package com.furnisight.notification.domain.model.base;

import com.furnisight.notification.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class DomainEntity {

    protected DomainEntity() {
    }

    // Domain Events
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
