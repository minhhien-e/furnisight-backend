package com.furnisight.notification.application.notification.port.in.dto.command;

import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
public class ValidateNotificationProfileCommand {
    private UUID userId;
    private NotificationType notificationType;
}
