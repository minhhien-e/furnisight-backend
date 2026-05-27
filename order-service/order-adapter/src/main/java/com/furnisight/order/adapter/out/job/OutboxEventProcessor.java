package com.furnisight.order.adapter.out.job;

import com.furnisight.order.domain.entities.OutboxMessage;
import com.furnisight.order.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {
        List<OutboxMessage> messages = outboxMessageRepository.findPendingMessages(50);
        if (messages.isEmpty()) {
            return;
        }

        for (OutboxMessage message : messages) {
            try {
                String topic = message.getType();
                kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload());
                message.markAsProcessed();
            } catch (Exception e) {
                message.markAsFailed(e.getMessage());
            }
            outboxMessageRepository.save(message);
        }
    }
}
