package com.furnisight.user.domain.seedwork;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MappedSuperclass
public abstract class AggregateRoot extends BaseEntity {

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public void registerEvent(DomainEvent event) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(event);
    }

    @DomainEvents
    public List<DomainEvent> getDomainEvents() {
        return domainEvents == null ? Collections.emptyList() : Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        if (domainEvents != null) {
            domainEvents.clear();
        }
    }
}
