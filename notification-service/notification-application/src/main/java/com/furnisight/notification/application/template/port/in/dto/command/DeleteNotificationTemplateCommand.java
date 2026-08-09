package com.furnisight.notification.application.template.port.in.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteNotificationTemplateCommand {
    private UUID templateId;
}
