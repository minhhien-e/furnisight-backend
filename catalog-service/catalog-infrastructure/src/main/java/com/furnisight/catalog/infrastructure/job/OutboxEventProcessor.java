package com.furnisight.catalog.infrastructure.job;

import com.furnisight.catalog.application.common.port.out.EventPublisher;
import com.furnisight.catalog.domain.entities.OutboxMessage;
import com.furnisight.catalog.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxMessageRepository outboxMessageRepository;
    private final EventPublisher eventPublisher;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {
        List<OutboxMessage> messages = outboxMessageRepository.findPendingMessages();
        
        if (messages.isEmpty()) {
            return;
        }

        log.info("Processing {} pending outbox messages", messages.size());

        for (OutboxMessage message : messages) {
            try {
                String topic = message.getType();
                eventPublisher.publish(message.getPayload(), topic);
                message.markAsProcessed();
            } catch (Exception e) {
                log.error("Failed to process outbox message {}: {}", message.getId(), e.getMessage());
                message.markAsFailed(e.getMessage());
            }
            // Message state is updated and will be saved at the end of transaction
            outboxMessageRepository.save(message);
        }
    }
}
