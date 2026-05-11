package com.furnisight.notification.domain.event;

import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@lombok.ToString
@NoArgsConstructor
public class NotificationCreatedEvent implements DomainEvent {
    public final static String EVENT_TYPE = "notification-created";
    private UUID eventId;
    private LocalDateTime occurredOn;
    private String title;
    private String body;
    private String actionUrl;
    private NotificationType type;
    private UUID userId;
    private String image;

    @Builder
    public NotificationCreatedEvent(String title, String body, String actionUrl, NotificationType type, UUID userId, String image) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.title = title;
        this.body = body;
        this.actionUrl = actionUrl;
        this.type = type;
        this.userId = userId;
        this.image = image;
    }

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String type() {
        return EVENT_TYPE;
    }
}
