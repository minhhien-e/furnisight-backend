package com.furnisight.notification.application.template.port.in.usecase;

import com.furnisight.notification.application.template.port.in.dto.command.CreateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.projection.NotificationTemplateProjection;

public interface CreateNotificationTemplateUseCase {
    NotificationTemplateProjection execute(CreateNotificationTemplateCommand command);
}
