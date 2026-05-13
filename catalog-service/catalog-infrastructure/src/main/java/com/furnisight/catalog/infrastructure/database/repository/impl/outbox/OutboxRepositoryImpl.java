package com.furnisight.catalog.infrastructure.database.repository.impl.outbox;

import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.OutboxJpaRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.domain.repository.OutboxRepository;
import com.furnisight.catalog.domain.seedwork.DomainEvent;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxMessage;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxStatus;
import com.furnisight.catalog.domain.exceptions.OutboxEventProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxJpaRepository jpaOutboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveAll(String aggregateType, String aggregateId, List<DomainEvent> events) {
        for (DomainEvent event : events) {
            String payload;
            try {
                payload = objectMapper.writeValueAsString(event);
            } catch (JsonProcessingException e) {
                throw new OutboxEventProcessingException("Error serializing outbox event", e);
            }

            OutboxMessage msg = OutboxMessage.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(event.getClass().getSimpleName())
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            jpaOutboxRepository.save(msg);
        }
    }
}

