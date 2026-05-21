package com.furnisight.catalog.infrastructure.event;

import com.furnisight.catalog.application.common.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherImpl implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(String payload, String topic) {
        kafkaTemplate.send(topic, payload);
    }
}
