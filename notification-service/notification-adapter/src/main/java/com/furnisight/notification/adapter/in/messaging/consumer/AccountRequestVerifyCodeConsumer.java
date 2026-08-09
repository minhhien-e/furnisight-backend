package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.AccountVerificationRequestedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountRequestVerifyCodeConsumer extends AbstractNotificationConsumer<AccountVerificationRequestedEvent> {
    private final static String TOPIC = "account-verification-requested";
    private final static String TEMPLATE_CODE = "account-verify-code";

    public AccountRequestVerifyCodeConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, AccountVerificationRequestedEvent.class, TEMPLATE_CODE);
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(AccountVerificationRequestedEvent event) {
        return event.accountId();
    }

    @Override
    protected String getDestination(AccountVerificationRequestedEvent event) {
        return event.destination();
    }

    @Override
    protected NotificationChannel getChannel(AccountVerificationRequestedEvent event) {
        return NotificationChannel.EMAIL;
    }
}
