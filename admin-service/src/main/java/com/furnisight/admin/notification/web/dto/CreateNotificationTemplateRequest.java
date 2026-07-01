package com.furnisight.admin.notification.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationTemplateRequest {
    @NotBlank(message = "Code is required")
    private String code;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Title template is required")
    private String titleTemplate;
    @NotBlank(message = "Body template is required")
    private String bodyTemplate;
    @NotNull(message = "Type is required")
    private NotificationType type;
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    private String defaultImage;
    private String defaultActionUrl;
}
