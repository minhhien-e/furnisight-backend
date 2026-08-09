package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.SocialAccountCreatedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SocialAccountCreatedConsumer extends AbstractNotificationConsumer<SocialAccountCreatedEvent> {
    private final static String TOPIC = "social-account-created";
    private final static String TEMPLATE_CODE = "social-account-created";

    public SocialAccountCreatedConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, SocialAccountCreatedEvent.class, TEMPLATE_CODE);
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(SocialAccountCreatedEvent event) {
        return event.accountId();
    }

    @Override
    protected NotificationChannel getChannel(SocialAccountCreatedEvent event) {
        return NotificationChannel.EMAIL;
    }
}
