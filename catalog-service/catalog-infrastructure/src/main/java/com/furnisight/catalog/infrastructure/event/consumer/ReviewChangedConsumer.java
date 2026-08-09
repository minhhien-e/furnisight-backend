package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductReviewStatsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewChangedConsumer {

    private static final String TOPIC = "review-changed";
    private final ObjectMapper objectMapper;
    private final UpdateProductReviewStatsUseCase updateProductReviewStatsUseCase;

    @KafkaListener(topics = TOPIC)
    public void consume(String message) {
        log.info("Received message on topic {}: {}", TOPIC, message);
        try {
            JsonNode payload = objectMapper.readTree(message);
            if (payload.has("productId")) {
                UUID productId = UUID.fromString(payload.get("productId").asText());
                updateProductReviewStatsUseCase.execute(productId);
            }
        } catch (Exception e) {
            log.error("Failed to process {} event", TOPIC, e);
        }
    }
}
