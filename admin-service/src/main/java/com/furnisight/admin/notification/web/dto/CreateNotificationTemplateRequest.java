package com.furnisight.admin.notification.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationTemplateRequest {
    private String code;
    private String name;
    private String titleTemplate;
    private String bodyTemplate;
    private NotificationType type;
    private NotificationChannel channel;
    private String defaultImage;
    private String defaultActionUrl;
}
