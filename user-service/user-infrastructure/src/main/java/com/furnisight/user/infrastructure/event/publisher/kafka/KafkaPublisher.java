package com.furnisight.user.infrastructure.event.publisher.kafka;

import com.furnisight.user.application.common.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class KafkaPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(String payload, String topic) {
        kafkaTemplate.send(topic, payload);
    }
}
