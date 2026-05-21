package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.command.RecordProductFavoriteLogCommand;
import com.furnisight.catalog.application.product.port.in.usecase.RecordProductFavoriteLogUseCase;
import com.furnisight.catalog.infrastructure.event.dto.ProductFavoritedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFavoritedConsumer {
    private static final String TOPIC = "product-favorited";

    private final RecordProductFavoriteLogUseCase recordProductFavoriteLogUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        ProductFavoritedEvent event = objectMapper.readValue(payload, ProductFavoritedEvent.class);
        recordProductFavoriteLogUseCase.execute(RecordProductFavoriteLogCommand.builder()
            .userId(event.accountId())
            .productId(event.productId())
            .build());
    }
}
