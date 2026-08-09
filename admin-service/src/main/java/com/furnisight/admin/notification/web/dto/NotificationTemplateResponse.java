package com.furnisight.admin.notification.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateResponse {
    private UUID id;
    private String code;
    private String name;
    private List<String> variables;
    private String titleTemplate;
    private String bodyTemplate;
    private NotificationType type;
    private NotificationChannel channel;
    private String defaultImage;
    private String defaultActionUrl;
}
