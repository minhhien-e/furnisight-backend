package com.furnisight.catalog.infrastructure.event.publisher.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.domain.events.category.CategoryCreateEvent;
import com.furnisight.catalog.domain.events.category.CategoryUpdateEvent;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.OutboxJpaRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryEventToOutboxListener {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Category";

    @EventListener
    public void handle(CategoryCreateEvent event) {
        saveToOutbox(event.getCategoryId().toString(), "CATEGORY_CREATED", event);
    }

    @EventListener
    public void handle(CategoryUpdateEvent event) {
        saveToOutbox(event.getCategoryId().toString(), "CATEGORY_UPDATED", event);
    }

    private void saveToOutbox(String aggregateId, String eventType, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxMessage message = new OutboxMessage(
                AGGREGATE_TYPE,
                aggregateId,
                eventType,
                payload
            );
            outboxRepository.save(message);
            log.info("Saved outbox message for aggregate {} (Type: {})", aggregateId, eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event: {}", e.getMessage(), e);
        }
    }
}
