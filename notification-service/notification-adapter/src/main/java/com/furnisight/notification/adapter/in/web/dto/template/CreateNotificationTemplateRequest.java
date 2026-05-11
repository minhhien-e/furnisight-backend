package com.furnisight.notification.adapter.in.web.dto.template;

import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationTemplateRequest {
    @NotBlank(message = "Code is mandatory")
    private String code;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Title template is mandatory")
    private String titleTemplate;

    @NotBlank(message = "Body template is mandatory")
    private String bodyTemplate;

    @NotNull(message = "Notification type is mandatory")
    private NotificationType type;

    @NotNull(message = "Notification channel is mandatory")
    private NotificationChannel channel;

    private String defaultImage;
    private String defaultActionUrl;
}
