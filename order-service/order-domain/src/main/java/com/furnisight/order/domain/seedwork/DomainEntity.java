package com.furnisight.order.domain.seedwork;

import com.furnisight.order.domain.events.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class DomainEntity {

    protected DomainEntity() {
    }

    @jakarta.persistence.Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    @org.springframework.data.domain.DomainEvents
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    @org.springframework.data.domain.AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
