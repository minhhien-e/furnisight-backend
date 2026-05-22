package com.furnisight.user.infrastructure.event.publisher.favorite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.user.domain.entities.OutboxMessage;
import com.furnisight.user.domain.events.favorite.ProductFavoritedEvent;
import com.furnisight.user.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteProductEventToOutboxListener {
    private static final String AGGREGATE_TYPE = "FavoriteProduct";
    private static final String PRODUCT_FAVORITED_TOPIC = "product-favorited";

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @EventListener
    public void handle(ProductFavoritedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.favoriteId().toString(),
            PRODUCT_FAVORITED_TOPIC,
            payload
        ));
    }

    @SneakyThrows
    @EventListener
    public void handle(com.furnisight.user.domain.events.favorite.ProductUnfavoritedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        outboxMessageRepository.save(new OutboxMessage(
            AGGREGATE_TYPE,
            event.id().toString(),
            "product-unfavorited",
            payload
        ));
    }
}
