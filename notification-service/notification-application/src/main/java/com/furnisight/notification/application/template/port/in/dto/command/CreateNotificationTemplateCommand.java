package com.furnisight.notification.application.template.port.in.dto.command;

import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationTemplateCommand {
    private String code;
    private String name;
    private String titleTemplate;
    private String bodyTemplate;
    private NotificationType type;
    private NotificationChannel channel;
    private String defaultImage;
    private String defaultActionUrl;
}
