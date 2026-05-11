package com.furnisight.notification.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furnisight.notification.adapter.in.messaging.dto.event.MediaUploadedEvent;
import com.furnisight.notification.application.notification.port.in.dto.command.RenderNotificationCommand;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.in.usecase.RenderNotificationUseCase;
import com.furnisight.notification.application.notification.port.in.usecase.SendNotificationUseCase;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MediaUploadedConsumer {
    /// Kafka
    private final static String TOPIC = "media-uploaded";
    /// Template
    private final static String TEMPLATE_CODE = "media-uploaded";
    /// Notification
    private final static NotificationChannel CHANNEL = NotificationChannel.IN_APP;
    private final static NotificationType TYPE = NotificationType.MEDIA;
    /// UseCase
    private final SendNotificationUseCase sendNotificationUseCase;
    private final RenderNotificationUseCase renderNotificationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC)
    @SuppressWarnings("unchecked")
    public void handle(MediaUploadedEvent event) {
        var renderResult = renderNotificationUseCase.execute(RenderNotificationCommand.builder()
            .templateCode(TEMPLATE_CODE)
            .data(objectMapper.convertValue(event, Map.class)).build());
        sendNotificationUseCase.execute(SendNotificationCommand.builder()
            .userId(event.getReceiverId())
            .title(renderResult.getTitle())
            .body(renderResult.getBody())
            .image("")
            .actionUrl("")
            .type(TYPE)
            .channel(CHANNEL)
            .build());
    }
}
