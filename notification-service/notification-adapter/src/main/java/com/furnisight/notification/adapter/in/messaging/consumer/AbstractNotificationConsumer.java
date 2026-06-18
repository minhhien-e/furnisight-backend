package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.TemplateEventData;
import com.furnisight.notification.application.notification.port.in.dto.command.RenderNotificationCommand;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class AbstractNotificationConsumer<T> {
    private final SendNotificationUseCase sendNotificationUseCase;
    private final RenderNotificationUseCase renderNotificationUseCase;
    private final ObjectMapper objectMapper;
    private final Class<T> eventClass;
    private final String templateCode;

    protected void processEvent(String payload) throws JsonProcessingException {
        T event = objectMapper.readValue(normalizePayload(payload), eventClass);
        var renderResult = renderNotificationUseCase.execute(RenderNotificationCommand.builder()
            .templateCode(templateCode)
            .data(TemplateEventData.from(objectMapper, event)).build());

        sendNotificationUseCase.execute(SendNotificationCommand.builder()
            .userId(getAccountId(event))
            .destination(getDestination(event))
            .title(renderResult.getTitle())
            .body(renderResult.getBody())
            .image("")
            .actionUrl("")
            .type(NotificationType.SYSTEM)
            .channel(getChannel(event))
            .build());
    }

    protected abstract UUID getAccountId(T event);

    /** Địa chỉ nhận trực tiếp từ event (email hoặc phone). Trả null nếu consumer không cung cấp. */
    protected String getDestination(T event) {
        return null;
    }

    protected NotificationChannel getChannel(T event) {
        return NotificationChannel.EMAIL; // default to email, can be overridden
    }

    private String normalizePayload(String payload) throws JsonProcessingException {
        var node = objectMapper.readTree(payload);
        return node.isTextual() ? node.asText() : payload;
    }
}
