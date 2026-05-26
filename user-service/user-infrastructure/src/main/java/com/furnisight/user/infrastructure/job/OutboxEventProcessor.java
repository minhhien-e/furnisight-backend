package com.furnisight.user.infrastructure.job;

import com.furnisight.user.application.common.port.out.EventPublisher;
import com.furnisight.user.domain.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final OutboxMessageRepository outboxMessageRepository;
    private final EventPublisher eventPublisher;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {
        var events = outboxMessageRepository.findPendingMessages();
        if (events.isEmpty()) {
            log.debug("No pending events found");
        } else {
            log.debug("Processing {} pending events", events.size());
        }
        for (var event : events) {
            try {
                eventPublisher.publish(event.getPayload(), event.getType());
            } catch (Exception e) {
                event.markAsFailed(e.getMessage());
            }
            event.markAsProcessed();
        }
    }
}
