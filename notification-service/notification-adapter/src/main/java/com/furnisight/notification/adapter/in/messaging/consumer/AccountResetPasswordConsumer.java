package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.AccountResetPasswordRequestedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountResetPasswordConsumer extends AbstractNotificationConsumer<AccountResetPasswordRequestedEvent> {
    private final static String TOPIC = "account-reset-password-requested";
    private final static String TEMPLATE_CODE = "account-reset-password";

    public AccountResetPasswordConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, AccountResetPasswordRequestedEvent.class, TEMPLATE_CODE);
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(AccountResetPasswordRequestedEvent event) {
        return event.accountId();
    }

    @Override
    protected String getDestination(AccountResetPasswordRequestedEvent event) {
        return event.destination();
    }
}
