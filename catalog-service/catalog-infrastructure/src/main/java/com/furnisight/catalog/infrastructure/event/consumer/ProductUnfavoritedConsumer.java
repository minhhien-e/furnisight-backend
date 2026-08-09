package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.dto.command.RecordProductFavoriteLogCommand;
import com.furnisight.catalog.application.product.port.in.usecase.RemoveProductFavoriteLogUseCase;
import com.furnisight.catalog.infrastructure.event.dto.ProductUnfavoritedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductUnfavoritedConsumer {
    private static final String TOPIC = "product-unfavorited";

    private final RemoveProductFavoriteLogUseCase removeProductFavoriteLogUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        ProductUnfavoritedEvent event = objectMapper.readValue(payload, ProductUnfavoritedEvent.class);
        removeProductFavoriteLogUseCase.execute(RecordProductFavoriteLogCommand.builder()
            .userId(event.accountId())
            .productId(event.productId())
            .build());
    }
}
