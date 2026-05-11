package com.furnisight.notification.application.template.port.in.dto.projection;

import com.furnisight.notification.domain.model.entity.NotificationTemplate;
import com.furnisight.notification.domain.model.enums.NotificationChannel;
import com.furnisight.notification.domain.model.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class NotificationTemplateProjection {
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

    public static NotificationTemplateProjection from(NotificationTemplate entity) {
        if (entity == null) return null;
        return NotificationTemplateProjection.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .variables(entity.getVariables())
            .titleTemplate(entity.getTitleTemplate())
            .bodyTemplate(entity.getBodyTemplate())
            .type(entity.getType())
            .channel(entity.getChannel())
            .defaultImage(entity.getDefaultImage())
            .defaultActionUrl(entity.getDefaultActionUrl())
            .build();
    }
}
