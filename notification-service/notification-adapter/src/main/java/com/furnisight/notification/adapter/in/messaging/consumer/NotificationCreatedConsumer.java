package com.furnisight.notification.adapter.in.messaging.consumer;

import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;
import com.furnisight.notification.application.notification.port.out.sender.NotificationSender;
import com.furnisight.notification.domain.event.NotificationCreatedEvent;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationCreatedConsumer {
    /// Kafka
    private final static String TOPIC = NotificationCreatedEvent.EVENT_TYPE;
    /// Sender
    private final NotificationSender notificationSender;

    public NotificationCreatedConsumer(@Qualifier("emailNotificationSender") NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    @KafkaListener(topics = TOPIC)
    public void handle(NotificationCreatedEvent event) {
        notificationSender.send(SendNotificationCommand.builder()
            .userId(event.getUserId())
            .title(event.getTitle())
            .body(event.getBody())
            .image(event.getImage())
            .actionUrl(event.getActionUrl())
            .type(event.getType())
            .channel(NotificationChannel.EMAIL)
            .build());
    }
}
