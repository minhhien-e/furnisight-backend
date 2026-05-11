package com.furnisight.notification.application.notification.port.in.usecase;

import com.furnisight.notification.application.common.port.in.UseCase;
import com.furnisight.notification.application.notification.port.in.dto.command.SendNotificationCommand;

public interface SendNotificationUseCase extends UseCase<SendNotificationCommand, Void> {
}
