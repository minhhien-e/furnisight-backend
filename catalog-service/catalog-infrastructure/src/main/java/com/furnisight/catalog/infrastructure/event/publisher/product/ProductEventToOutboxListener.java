package com.furnisight.catalog.infrastructure.event.publisher.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.domain.entities.OutboxMessage;
import com.furnisight.catalog.domain.events.product.ProductCreatedEvent;
import com.furnisight.catalog.domain.events.product.ProductStatusUpdatedEvent;
import com.furnisight.catalog.domain.events.product.ProductUpdatedEvent;
import com.furnisight.catalog.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventToOutboxListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Product";

    @SneakyThrows
    @EventListener
    public void handle(ProductCreatedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.getProductId().toString(),
            "product-created",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(ProductUpdatedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.getProductId().toString(),
            "product-updated",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(ProductStatusUpdatedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.getProductId().toString(),
            "product-status-updated",
            payload
        ));
    }
}
