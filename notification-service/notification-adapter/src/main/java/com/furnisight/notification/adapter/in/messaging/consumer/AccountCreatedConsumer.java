package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.AccountCreatedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.application.profile.port.in.command.CreateDefaultNotificationProfileCommand;
import com.furnisight.notification.application.profile.port.in.usecase.CreateDefaultNotificationProfileUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountCreatedConsumer extends AbstractNotificationConsumer<AccountCreatedEvent> {
    private static final String TOPIC = "account-created";
    private static final String TEMPLATE_CODE = "account-created";

    private final CreateDefaultNotificationProfileUseCase createDefaultNotificationProfileUseCase;
    private final ObjectMapper objectMapper;

    public AccountCreatedConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper,
            CreateDefaultNotificationProfileUseCase createDefaultNotificationProfileUseCase) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, AccountCreatedEvent.class, TEMPLATE_CODE);
        this.createDefaultNotificationProfileUseCase = createDefaultNotificationProfileUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        AccountCreatedEvent event = objectMapper.readValue(payload, AccountCreatedEvent.class);
        
        // 1. Setup notification profile
        createDefaultNotificationProfileUseCase.execute(new CreateDefaultNotificationProfileCommand(event.accountId()));
        
        // 2. Send Welcome Email
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(AccountCreatedEvent event) {
        return event.accountId();
    }

    @Override
    protected String getDestination(AccountCreatedEvent event) {
        return event.email();
    }

    @Override
    protected NotificationChannel getChannel(AccountCreatedEvent event) {
        return NotificationChannel.EMAIL;
    }
}
