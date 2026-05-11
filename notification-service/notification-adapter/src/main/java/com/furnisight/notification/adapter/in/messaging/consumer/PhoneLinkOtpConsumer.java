package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.PhoneLinkOtpRequestedEvent;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PhoneLinkOtpConsumer extends AbstractNotificationConsumer<PhoneLinkOtpRequestedEvent> {
    private final static String TOPIC = "phone-link-otp-requested";
    private final static String TEMPLATE_CODE = "account-phone-link";

    public PhoneLinkOtpConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            RenderNotificationUseCase renderNotificationUseCase,
            ObjectMapper objectMapper) {
        super(sendNotificationUseCase, renderNotificationUseCase, objectMapper, PhoneLinkOtpRequestedEvent.class, TEMPLATE_CODE);
    }

    @KafkaListener(topics = TOPIC)
    public void handle(String payload) throws JsonProcessingException {
        processEvent(payload);
    }

    @Override
    protected UUID getAccountId(PhoneLinkOtpRequestedEvent event) {
        return event.accountId();
    }

    @Override
    protected String getDestination(PhoneLinkOtpRequestedEvent event) {
        return event.destination();
    }
}
