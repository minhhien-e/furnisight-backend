package com.furnisight.notification.adapter.in.messaging.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record MarketingNotificationRequestedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String destination,
        String title,
        String body,
        String actionUrl,
        String channel) {
}
