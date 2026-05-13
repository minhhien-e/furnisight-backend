package com.furnisight.catalog.domain.repository;

import com.furnisight.catalog.domain.seedwork.DomainEvent;

import java.util.List;

public interface OutboxRepository {
    void saveAll(String aggregateType, String aggregateId, List<DomainEvent> events);
}

