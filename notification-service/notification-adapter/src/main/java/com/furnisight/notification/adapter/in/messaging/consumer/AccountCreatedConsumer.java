package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.AccountCreatedEvent;
import com.furnisight.notification.application.profile.port.in.command.CreateDefaultNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.usecase.CreateDefaultNotificationProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountCreatedConsumer {
    private static final String TOPIC = "account-created";

    private final CreateDefaultNotificationProfileUseCase createDefaultNotificationProfileUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        AccountCreatedEvent event = objectMapper.readValue(payload, AccountCreatedEvent.class);
        createDefaultNotificationProfileUseCase.execute(new CreateDefaultNotificationProfileCommand(event.accountId()));
    }
}
