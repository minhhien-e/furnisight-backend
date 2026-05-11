package com.furnisight.notification.application.template.port.in.dto.query;

import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterNotificationTemplateQuery {
    private final String name;
    private final NotificationChannel channel;
    private final NotificationType type;
}
