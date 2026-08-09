package com.furnisight.catalog.infrastructure.event.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.catalog.application.product.port.in.usecase.UpdateProductSoldCountUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeliveredConsumer {

    private static final String TOPIC = "order-delivered";
    private final ObjectMapper objectMapper;
    private final UpdateProductSoldCountUseCase updateProductSoldCountUseCase;

    @KafkaListener(topics = TOPIC)
    public void consume(String message) {
        log.info("Received message on topic {}: {}", TOPIC, message);
        try {
            JsonNode payload = objectMapper.readTree(message);
            if (payload.has("items") && payload.get("items").isArray()) {
                Map<UUID, Integer> productQuantities = new HashMap<>();
                for (JsonNode item : payload.get("items")) {
                    if (item.has("productId") && item.has("quantity")) {
                        UUID productId = UUID.fromString(item.get("productId").asText());
                        int quantity = item.get("quantity").asInt();
                        productQuantities.put(productId, productQuantities.getOrDefault(productId, 0) + quantity);
                    }
                }
                updateProductSoldCountUseCase.execute(productQuantities);
            }
        } catch (Exception e) {
            log.error("Failed to process {} event", TOPIC, e);
        }
    }
}
