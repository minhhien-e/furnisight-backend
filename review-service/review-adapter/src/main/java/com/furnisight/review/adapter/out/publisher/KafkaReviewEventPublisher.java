package com.furnisight.review.adapter.out.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.review.application.review.port.out.ReviewEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaReviewEventPublisher implements ReviewEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishReviewChangedEvent(UUID productId) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of("productId", productId.toString()));
            kafkaTemplate.send("review-changed", productId.toString(), payload);
            log.info("Published review-changed event for product {}", productId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize review-changed event for product {}", productId, e);
        }
    }
}
