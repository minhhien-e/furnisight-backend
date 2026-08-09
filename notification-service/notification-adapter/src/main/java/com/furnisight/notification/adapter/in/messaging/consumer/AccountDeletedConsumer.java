package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.AccountDeletedEvent;
import com.furnisight.notification.application.profile.port.in.command.DeleteNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.usecase.DeleteNotificationProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountDeletedConsumer {
    private static final String TOPIC = "account-deleted";

    private final DeleteNotificationProfileUseCase deleteNotificationProfileUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        AccountDeletedEvent event = objectMapper.readValue(payload, AccountDeletedEvent.class);
        deleteNotificationProfileUseCase.execute(new DeleteNotificationProfileCommand(event.accountId()));
    }
}
