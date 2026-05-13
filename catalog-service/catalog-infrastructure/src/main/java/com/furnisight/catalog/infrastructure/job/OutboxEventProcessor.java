package com.furnisight.catalog.infrastructure.job;

import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxMessage;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.entity.OutboxStatus;
import com.furnisight.catalog.infrastructure.database.repository.jpa.outbox.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "5000")
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> messages = outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (messages.isEmpty()) {
            return;
        }

        log.info("Found {} outbox messages to process", messages.size());

        for (OutboxMessage message : messages) {
            try {
                String topic = message.getAggregateType().toLowerCase() + "-event";
                kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload()).get();

                message.setStatus(OutboxStatus.SENT);
                message.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(message);

                log.info("Sent outbox message ID {} (Type: {}) to Kafka", message.getId(), topic);

            } catch (Exception e) {
                log.error("Failed to send outbox message ID {} to Kafka: {}", message.getId(), e.getMessage(), e);

                message.setStatus(OutboxStatus.FAILED);
                message.setErrorMessage(e.getMessage());
                message.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(message);
            }
        }
    }
}

