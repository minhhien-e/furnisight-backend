package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.VerifyCurrentEmailOtpRequestedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VerifyCurrentEmailOtpConsumer extends AbstractNotificationConsumer<VerifyCurrentEmailOtpRequestedEvent> {
    private final static String TOPIC = "verify-current-email-otp-requested";
    private final static String TEMPLATE_CODE = "account-verify-current-email";

    public VerifyCurrentEmailOtpConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, VerifyCurrentEmailOtpRequestedEvent.class, TEMPLATE_CODE);
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(VerifyCurrentEmailOtpRequestedEvent event) {
        return event.accountId();
    }

    @Override
    protected String getDestination(VerifyCurrentEmailOtpRequestedEvent event) {
        return event.destination();
    }
}
