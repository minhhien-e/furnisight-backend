package com.furnisight.catalog.infrastructure.event.publisher.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.domain.entities.OutboxMessage;
import com.furnisight.catalog.domain.events.category.CategoryCreateEvent;
import com.furnisight.catalog.domain.events.category.CategoryUpdateEvent;
import com.furnisight.catalog.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryEventToOutboxListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private static final String AGGREGATE_TYPE = "Category";

    @SneakyThrows
    @EventListener
    public void handle(CategoryCreateEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.getCategoryId().toString(),
            "category-created",
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(CategoryUpdateEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.getCategoryId().toString(),
            "category-updated",
            payload
        ));
    }
}
