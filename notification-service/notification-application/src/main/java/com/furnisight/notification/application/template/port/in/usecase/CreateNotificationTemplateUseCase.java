package com.furnisight.notification.application.template.port.in.usecase;

import com.furnisight.notification.application.template.port.in.dto.command.CreateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.response.NotificationTemplateResponse;

public interface CreateNotificationTemplateUseCase {
    NotificationTemplateResponse execute(CreateNotificationTemplateCommand command);
}
