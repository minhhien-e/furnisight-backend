package com.furnisight.catalog.infrastructure.event.publisher;

import com.furnisight.catalog.application.common.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(String payload, String topic) {
        try {
            kafkaTemplate.send(topic, payload).get();
            log.info("Published message to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to publish message to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish message to Kafka", e);
        }
    }
}
