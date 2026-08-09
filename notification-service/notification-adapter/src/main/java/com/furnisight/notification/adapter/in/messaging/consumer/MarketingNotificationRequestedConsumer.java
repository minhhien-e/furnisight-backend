package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.MarketingNotificationRequestedEvent;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketingNotificationRequestedConsumer {
    private static final String TOPIC = "marketing-notification-requested";

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) {
        try {
            var event = objectMapper.readValue(normalizePayload(payload), MarketingNotificationRequestedEvent.class);
            sendNotificationUseCase.execute(SendNotificationCommand.builder()
                    .userId(event.userId())
                    .destination(event.destination())
                    .title(event.title())
                    .body(event.body())
                    .image("")
                    .actionUrl(event.actionUrl())
                    .type(NotificationType.PROMOTION)
                    .channel(NotificationChannel.valueOf(event.channel()))
                    .metadata(event.metadata())
                    .build());
        } catch (Exception ex) {
            log.error("Failed to process marketing notification", ex);
            throw new IllegalStateException("Failed to process marketing notification", ex);
        }
    }

    private String normalizePayload(String payload) throws com.fasterxml.jackson.core.JsonProcessingException {
        var node = objectMapper.readTree(payload);
        return node.isTextual() ? node.asText() : payload;
    }
}
