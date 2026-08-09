package com.furnisight.notification.application.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.application.outbox.port.in.usecase.AddOutboxEventUseCase;
import com.furnisight.notification.application.outbox.port.out.repository.OutboxEventRepository;
import com.furnisight.notification.domain.event.DomainEvent;
import com.furnisight.notification.domain.model.entity.OutboxEvent;
import com.furnisight.notification.domain.model.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddOutboxEventService implements AddOutboxEventUseCase {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void addDomainEvents(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<OutboxEvent> outboxEvents = events.stream()
            .map(this::toOutboxEvent)
            .collect(Collectors.toList());

        outboxEventRepository.saveAll(outboxEvents);
    }

    private OutboxEvent toOutboxEvent(DomainEvent event) {
        try {
            return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("Notification")
                .aggregateId(event.eventId().toString())
                .eventType(event.type())
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }
}
