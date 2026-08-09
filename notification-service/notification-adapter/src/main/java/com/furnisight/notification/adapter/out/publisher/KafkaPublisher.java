package com.furnisight.notification.adapter.out.publisher;

import com.furnisight.notification.domain.event.DomainEvent;
import com.furnisight.notification.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPublisher implements EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(DomainEvent event) {
        String topic = resolveTopic(event);
        kafkaTemplate.send(topic, event);
    }

    private String resolveTopic(DomainEvent event) {
        return event.type();
    }
}
