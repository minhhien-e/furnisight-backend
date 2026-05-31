package com.furnisight.user.infrastructure.messaging.publisher;

import com.furnisight.user.application.common.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(String payload, String topic, String eventId) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, eventId, payload);
            record.headers().add("event-id", eventId.getBytes(StandardCharsets.UTF_8));
            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish event " + eventId + " to topic " + topic, e);
        }
    }
}
