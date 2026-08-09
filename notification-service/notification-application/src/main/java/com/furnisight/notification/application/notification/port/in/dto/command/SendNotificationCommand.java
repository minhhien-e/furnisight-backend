package com.furnisight.notification.application.notification.port.in.dto.command;

import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class SendNotificationCommand {
    private UUID userId;
    private String destination; // email address from the event
    private String title;
    private String body;
    private String image;
    private String actionUrl;
    private NotificationType type;
    private NotificationChannel channel;
    private Map<String, Object> metadata;
}
