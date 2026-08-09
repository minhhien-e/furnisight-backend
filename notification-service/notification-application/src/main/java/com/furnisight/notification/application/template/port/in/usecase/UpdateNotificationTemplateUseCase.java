package com.furnisight.notification.application.template.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.template.port.in.dto.command.UpdateNotificationTemplateCommand;
import com.furnisight.notification.application.template.port.in.dto.response.NotificationTemplateResponse;

public interface UpdateNotificationTemplateUseCase extends UseCase<UpdateNotificationTemplateCommand, NotificationTemplateResponse> {
}
