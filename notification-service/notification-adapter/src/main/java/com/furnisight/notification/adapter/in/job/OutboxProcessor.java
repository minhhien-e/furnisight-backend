package com.furnisight.notification.adapter.in.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.application.outbox.port.out.repository.OutboxEventRepository;
import com.furnisight.notification.domain.event.DomainEvent;
import com.furnisight.notification.domain.event.EventPublisher;
import com.furnisight.notification.domain.event.NotificationCreatedEvent;
import com.furnisight.notification.domain.model.entity.OutboxEvent;
import com.furnisight.notification.domain.model.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Async
    @Scheduled(fixedDelay = 5000)
    public void process() {
        List<OutboxEvent> events = outboxEventRepository.findAllByStatus(OutboxStatus.PENDING, 10);
        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            DomainEvent domainEvent = toDomainEvent(event);
            eventPublisher.publish(domainEvent);
            event.setStatus(OutboxStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to process outbox event {}: {}", event.getId(), e.getMessage());
            int newRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(newRetryCount);

            if (newRetryCount > 3) {
                event.setStatus(OutboxStatus.FAILED);
                event.setErrorMessage(e.getMessage());
            } else {
                event.setStatus(OutboxStatus.PENDING);
                event.setErrorMessage(e.getMessage());
            }
        } finally {
            outboxEventRepository.save(event);
        }
    }

    private DomainEvent toDomainEvent(OutboxEvent event) throws JsonProcessingException {
        if (event.getEventType().equals(NotificationCreatedEvent.EVENT_TYPE)) {
            return objectMapper.readValue(event.getPayload(), NotificationCreatedEvent.class);
        }
        return null;
    }
}
