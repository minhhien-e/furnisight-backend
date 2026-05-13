package com.furnisight.catalog.infrastructure.event.publisher.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.domain.events.product.ProductCreatedEvent;
import com.furnisight.catalog.domain.events.product.ProductStatusUpdatedEvent;
import com.furnisight.catalog.domain.events.product.ProductUpdatedEvent;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.OutboxJpaRepository;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventToOutboxListener {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Product";

    @EventListener
    public void handle(ProductCreatedEvent event) {
        saveToOutbox(event.getProductId().toString(), "PRODUCT_CREATED", event);
    }

    @EventListener
    public void handle(ProductUpdatedEvent event) {
        saveToOutbox(event.getProductId().toString(), "PRODUCT_UPDATED", event);
    }

    @EventListener
    public void handle(ProductStatusUpdatedEvent event) {
        saveToOutbox(event.getProductId().toString(), "PRODUCT_STATUS_UPDATED", event);
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
