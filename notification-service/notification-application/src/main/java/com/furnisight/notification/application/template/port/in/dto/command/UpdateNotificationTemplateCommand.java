package com.furnisight.notification.application.template.port.in.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UpdateNotificationTemplateCommand {
    private final UUID templateId;
    private final String name;
    private final String titleTemplate;
    private final String bodyTemplate;
}
